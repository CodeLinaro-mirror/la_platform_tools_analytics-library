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

package com.android.tools.analytics

import com.android.annotations.VisibleForTesting
import com.android.utils.ILogger
import java.nio.file.Paths
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Base class for publishing analytics. This class has two subclasses, one that publishes
 * analytics to Google's servers for users who opted in to metrics and one that is a Noop to ensure
 * metrics never get published for users who opt out.
 */
abstract class AnalyticsPublisher protected constructor() : AutoCloseable {
  /** Gets the interval in nano-seconds used for scheduling jobs to publish metrics.  */
  var publishInterval = TimeUnit.MINUTES.toNanos(10)
    private set

  /**
   * Sets the interval used for scheduling jobs to publish metrics.
   */
  open fun setPublishInterval(interval: Long, unit: TimeUnit) {
    publishInterval = unit.toNanos(interval)
  }

  companion object {
    private var instance_: AnalyticsPublisher = NullAnalyticsPublisher
    private val gate = Any()


    /**
     * Initializes the publisher retrieved by [.getInstance]
     *
     * @param analyticsSettings used to check opt-in vs opt-out status.
     * @param scheduler used to schedule jobs for publishing.
     * @param applicationBuild version information about the app publishing analytics.
     */
    @JvmStatic
    fun initialize(
      scheduler: ScheduledExecutorService,
      applicationBuild: String): AnalyticsPublisher {
      synchronized(gate) {
        if (AnalyticsSettings.optedIn && !AnalyticsSettings.debugDisablePublishing) {
          instance_ = GoogleAnalyticsPublisher(
            scheduler,
            Paths.get(AnalyticsPaths.spoolDirectory),
            applicationBuild)
        }
        else {
          instance_ = NullAnalyticsPublisher
        }
        return instance_
      }
    }

    /**
     * Retrieved the configured publisher based on a call to [.initialize]
     */
    @JvmStatic
    val instance: AnalyticsPublisher
      get() = synchronized(gate) {
        return instance_
      }

    /** Closes the current publisher and creates a new instance.  */
    @JvmStatic
    fun updatePublisher(
      logger: ILogger,
      scheduler: ScheduledExecutorService,
      applicationBuild: String) {
      AnalyticsSettings.initialize(logger)
      val current = instance
      try {
        current.close()
      }
      catch (e: Exception) {
        logger.error(e, "Unable to close existing analytics publisher")
      }

      initialize(scheduler, applicationBuild)
    }

    @VisibleForTesting
    fun setInstanceForTest(instance: AnalyticsPublisher) {
      this.instance_ = instance
    }
  }
}
