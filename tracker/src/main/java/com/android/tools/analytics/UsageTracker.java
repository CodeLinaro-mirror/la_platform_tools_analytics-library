/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.analytics;

import com.android.annotations.NonNull;
import com.android.annotations.VisibleForTesting;
import com.android.utils.DateProvider;
import com.android.utils.ILogger;
import com.google.wireless.android.play.playlog.proto.ClientAnalytics;
import com.google.wireless.android.sdk.stats.AndroidStudioEvent;
import com.google.wireless.android.sdk.stats.ProductDetails;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * UsageTracker is an api to report usage of features. This data is used to improve
 * future versions of Android Studio and related tools.
 *
 * The tracker has an API to logDetails usage (in the form of protobuf messages).
 * A separate system called the Analytics Publisher takes the logs and sends them
 * to Google's servers for analysis.
 */
public abstract class UsageTracker implements AutoCloseable {
    private static final Object sGate = new Object();

    @VisibleForTesting static String sSessionId = UUID.randomUUID().toString();
    @VisibleForTesting public static DateProvider sDateProvider = DateProvider.SYSTEM;
    private static UsageTracker sInstance = new NullUsageTracker(new AnalyticsSettings(), null);
    private static boolean sIsTesting;

    private final AnalyticsSettings mAnalyticsSettings;
    private final ScheduledExecutorService mScheduler;

    private int mMaxJournalSize;
    private long mMaxJournalTime;
    private String mVersion;
    private AndroidStudioEvent.IdeBrand mIdeBrand = AndroidStudioEvent.IdeBrand.UNKNOWN_IDE_BRAND;

    @VisibleForTesting protected long mStartTimeMs = sDateProvider.now().getTime();

    protected UsageTracker(
            AnalyticsSettings analyticsSettings, ScheduledExecutorService scheduler) {
        this.mAnalyticsSettings = analyticsSettings;
        this.mScheduler = scheduler;
    }
    /**
     * Indicates whether this UsageTracker has a maximum size at which point logs need to be flushed.
     * Zero or less indicates no maximum size at which to flush.
     */
    public int getMaxJournalSize() {
        return mMaxJournalSize;
    }

    /*
     * Sets a maximum size at which point logs need to be flushed. Zero or less indicates no
     * flushing until @{link #close()} is called.
     */
    public void setMaxJournalSize(int maxJournalSize) {
        this.mMaxJournalSize = maxJournalSize;
    }

    /**
     * Indicates whether this UsageTracker has a timeout at which point logs need to be flushed.
     * Zero or less indicates no timeout is set.
     *
     * @return timeout in nano-seconds.
     */
    public long getMaxJournalTime() {
        return mMaxJournalTime;
    }

    /**
     * Sets a timeout at which point logs need to be flushed. Zero or less indicates no timeout
     * should be used.
     */
    public void setMaxJournalTime(long duration, TimeUnit unit) {
        this.mMaxJournalTime = unit.toNanos(duration);
    }

    /**
     * Gets the version specified for this UsageTracker. This version when specified is used
     * to populate the product_details.version field of AndroidStudioEvent at time of logging
     * As the version of the product generating the event can be different of the version uploading
     * the event.
     */
    @NonNull public String getVersion() {
        return mVersion;
    }

    /**
     * Set the version specified for this UsageTracker. This version when specified is used
     * to populate the product_details.version field of AndroidStudioEvent at time of logging
     * As the version of the product generating the event can be different of the version uploading
     * the event.
     */
    public void setVersion(@NonNull String version) {
        mVersion = version;
    }

    /**
     * Gets the ide brand specified for this UsageTracker.
     */
    @NonNull public AndroidStudioEvent.IdeBrand getIdeBrand() {
        return mIdeBrand;
    }

    /**
     * Set the ide brand specified for this UsageTracker.
     */
    public void setIdeBrand(@NonNull AndroidStudioEvent.IdeBrand ideBrand) {
        mIdeBrand = ideBrand;
    }

    /** Gets the analytics settings used by this tracker. */
    public AnalyticsSettings getAnalyticsSettings() {
        return mAnalyticsSettings;
    }

    /** Gets the scheduler used by this tracker. */
    public ScheduledExecutorService getScheduler() {
        return mScheduler;
    }

    /** Logs usage data provided in the @{link AndroidStudioEvent}. */
    public void log(@NonNull AndroidStudioEvent.Builder studioEvent) {
      log(sDateProvider.now().getTime(), studioEvent);
    }

    /** Logs usage data provided in the @{link AndroidStudioEvent} with provided event time. */
    public void log(long eventTimeMs, @NonNull AndroidStudioEvent.Builder studioEvent) {
        studioEvent.setStudioSessionId(sSessionId);
        studioEvent.setIdeBrand(mIdeBrand);

        if (mVersion != null && !studioEvent.hasProductDetails()) {
            studioEvent.setProductDetails(ProductDetails.newBuilder().setVersion(mVersion));
        }

        try {
            logDetails(
                    ClientAnalytics.LogEvent.newBuilder()
                            .setEventTimeMs(eventTimeMs)
                            .setEventUptimeMs(eventTimeMs - mStartTimeMs)
                            .setSourceExtension(studioEvent.build().toByteString()));
        } catch (NullPointerException exception) {
            // TODO: Temporary fix for http://b.android.com/224994. We should remove this try-catch
            // block once there is a permanent fix.
            logDetails(
                    ClientAnalytics.LogEvent.newBuilder()
                            .setEventTimeMs(eventTimeMs)
                            .setEventUptimeMs(eventTimeMs - mStartTimeMs));
        }
    }

    /**
     * Logs usage data provided in the @{link ClientAnalytics.LogEvent}. Normally using {#log} is
     * preferred please talk to this code's author if you need {@link #logDetails} instead.
     */
    public abstract void logDetails(@NonNull ClientAnalytics.LogEvent.Builder logEvent);

    /**
     * Gets an instance of the {@link UsageTracker} that has been initialized correctly for this process.
     */
    @NonNull
    public static UsageTracker getInstance() {
        synchronized (sGate) {
            return sInstance;
        }
    }

    /**
     * Initializes a {@link UsageTracker} for use throughout this process based on user opt-in and
     * other settings.
     */
    public static UsageTracker initialize(
            @NonNull AnalyticsSettings analyticsSettings,
            @NonNull ScheduledExecutorService scheduler) {
        synchronized (sGate) {
            UsageTracker oldInstance = sInstance;
            if (analyticsSettings.hasOptedIn()) {
                try {
                   sInstance =
                       new JournalingUsageTracker(
                           analyticsSettings,
                           scheduler,
                           Paths.get(AnalyticsPaths.getSpoolDirectory()));
                } catch (RuntimeException ex) {
                    sInstance = new NullUsageTracker(analyticsSettings, scheduler);
                    sInstance.copySettingsFrom(oldInstance);
                    throw ex;
                }
            } else {
                sInstance = new NullUsageTracker(analyticsSettings, scheduler);
            }
            sInstance.copySettingsFrom(oldInstance);
            try {
                oldInstance.close();
            }
            catch (Exception ex) {
              throw new RuntimeException("Unable to close usage tracker", ex);
            }
            return sInstance;
        }
    }

    private void copySettingsFrom(UsageTracker tracker) {
        setIdeBrand(tracker.getIdeBrand());
        setMaxJournalSize(tracker.getMaxJournalSize());
        setMaxJournalTime(tracker.getMaxJournalTime(), TimeUnit.NANOSECONDS);
        setVersion(tracker.getVersion());
    }

    /**
     * Sets the global instance to the provided tracker so tests can provide their own UsageTracker
     * implementation. NOTE: Should only be used from tests.
     */
    @VisibleForTesting
    public static UsageTracker setInstanceForTest(@NonNull UsageTracker tracker) {
        sIsTesting = true;
        return sInstance = tracker;
    }

    /**
     * resets the global instance to the null usage tracker, to clean state in tests. NOTE: Should
     * only be used from tests.
     */
    @VisibleForTesting
    public static void cleanAfterTesting() {
        sIsTesting = false;
        sInstance = new NullUsageTracker(new AnalyticsSettings(), null);
    }

    public static AnalyticsSettings updateSettingsAndTracker(
            boolean optIn, @NonNull ILogger logger, @NonNull ScheduledExecutorService scheduler) {
        AnalyticsSettings settings = AnalyticsSettings.getInstance(logger);

        if (sIsTesting) {
            // Don't persist test settings or close tracker
            return settings;
        }

        if (optIn != settings.hasOptedIn()) {
            settings.setHasOptedIn(optIn);
            try {
                settings.saveSettings();
            } catch (IOException e) {
                logger.error(e, "Unable to save analytics settings");
            }
        }
        try {
          initialize(settings, scheduler);
        } catch (Exception e) {
            logger.error(e, "Unable to initialize analytics tracker");
        }
        return settings;
    }
}
