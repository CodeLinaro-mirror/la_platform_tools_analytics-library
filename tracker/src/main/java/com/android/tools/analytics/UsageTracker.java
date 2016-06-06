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
import com.google.wireless.android.sdk.stats.AndroidStudioStats;
import com.google.wireless.android.play.playlog.proto.ClientAnalytics;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

/**
 * UsageTracker is an api to report usage of features. This data is used to improve
 * future versions of Android Studio and related tools.
 *
 * The tracker has an API to logDetails usage (in the form of protobuf messages).
 * A separate system called the Analytics Publisher takes the logs and sends them
 * to Google's servers for analysis.
 */
public abstract class UsageTracker implements AutoCloseable {
    private int mMaxJournalSize;
    private long mMaxJournalTimeMinutes;
    private static Object sGate = new Object();
    private static UsageTracker sInstance;

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
     */
    public long getMaxJournalTime() {
        return mMaxJournalTimeMinutes;
    }

    /**
     * Sets a timeout at which point logs need to be flushed. Zero or less indicates no timeout
     * should be used.
     */
    public void setMaxJournalTime(long maxJournalTimeMinutes) {
        this.mMaxJournalTimeMinutes = maxJournalTimeMinutes;
    }

    /**
     * Logs usage data provided in the @{link AndroidStudioStats.AndroidStudioEvent}.
     */
    public void log(@NonNull AndroidStudioStats.AndroidStudioEvent.Builder studioEvent) {
        logDetails(
                ClientAnalytics.LogEvent.newBuilder()
                        .setEventTimeMs(new Date().getTime())
                        .setSourceExtension(studioEvent.build().toByteString()));
    }

    /**
     * Logs usage data provided in the @{link ClientAnalytics.LogEvent}. Normally using {#log} is
     * preferred please talk to this code's author if you need {@link #logDetails} instead.
     */
    public abstract void logDetails(@NonNull ClientAnalytics.LogEvent.Builder logEvent);

    /**
     * Gets an instance of the {@link UsageTracker} that has been initialized correctly for this process.
     */
    public static UsageTracker getInstance() {
        synchronized (sGate) {
            return sInstance;
        }
    }

    /**
     * Initializes a {@link UsageTracker} for use throughout this process based on user opt-in and
     * other settings.
     */
    public static void initialize(
            AnalyticsSettings analyticsSettings, ScheduledExecutorService eventLoop) {
        synchronized (sGate) {
            if (analyticsSettings.hasOptedIn()) {
                sInstance =
                        new JournalingUsageTracker(AnalyticsPaths.getSpoolDirectory(), eventLoop);
            } else {
                sInstance = new NullUsageTracker();
            }
        }
    }
}
