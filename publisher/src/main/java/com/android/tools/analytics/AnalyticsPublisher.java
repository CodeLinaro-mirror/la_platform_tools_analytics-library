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

import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * Base class for publishing analytics. This class has two subclasses, one that publishes
 * analytics to Google's servers for users who opted in to metrics and one that is a Noop to ensure
 * metrics never get published for users who opt out.
 */
public abstract class AnalyticsPublisher implements AutoCloseable {
    private static final Object sGate = new Object();
    private static AnalyticsPublisher sInstance;
    private long mPublishIntervalNanos = TimeUnit.MINUTES.toNanos(10);

    /**
     * Initializes the publisher retrieved by {@link #getInstance()}
     * @param analyticsSettings used to check opt-in vs opt-out status.
     * @param eventLoop used to schedule jobs for publishing.
     */
    public static void initialize(
            @NonNull AnalyticsSettings analyticsSettings,
            @NonNull ScheduledExecutorService eventLoop) {
        synchronized (sGate) {
            if (analyticsSettings.hasOptedIn()) {
                sInstance =
                        new GoogleAnalyticsPublisher(
                                analyticsSettings,
                                Paths.get(AnalyticsPaths.getSpoolDirectory()),
                                eventLoop);
            } else {
                sInstance = new NullAnalyticsPublisher();
            }
        }
    }

    /**
     * Retrieved the configured publisher based on a call to
     * {@link #initialize(AnalyticsSettings, ScheduledExecutorService)}
     */
    @NonNull
    public static AnalyticsPublisher getInstance() {
        synchronized (sGate) {
            return sInstance;
        }
    }

    /**
     * Sets the interval used for scheduling jobs to publish metrics.
     */
    public void setPublishInterval(long interval, TimeUnit unit) {
        mPublishIntervalNanos = unit.toNanos(interval);
    }

    /**
     * Gets the interval in nano-seconds used for scheduling jobs to publish metrics.
     */
    public long getPublishInterval() {
        return mPublishIntervalNanos;
    }
}
