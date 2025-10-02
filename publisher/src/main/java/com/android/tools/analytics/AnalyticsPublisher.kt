/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.analytics

import com.android.utils.ILogger
import com.google.common.annotations.VisibleForTesting
import java.nio.file.Paths
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Base class for publishing analytics. This class has two subclasses, one that publishes analytics
 * to Google's servers for users who opted in to metrics and one that is a Noop to ensure metrics
 * never get published for users who opt out.
 */
abstract class AnalyticsPublisher protected constructor() : AutoCloseable {

  /** Gets the interval in nano-seconds used for scheduling jobs to publish metrics. */
  var publishInterval = TimeUnit.MINUTES.toNanos(10)
    private set

  /** Sets the interval used for scheduling jobs to publish metrics. */
  open fun setPublishInterval(interval: Long, unit: TimeUnit) {
    publishInterval = unit.toNanos(interval)
  }

  companion object {

    private var anonymousInstance_: AnalyticsPublisher = NullAnalyticsPublisher()
    private var loginInstance_: AnalyticsPublisher = NullAnalyticsPublisher()
    private val gate = Any()

    /**
     * Initializes the publisher retrieved by [.getInstance]
     *
     * @param scheduler used to schedule jobs for publishing.
     * @param applicationBuild version information about the app publishing analytics.
     */
    @JvmStatic
    fun initialize(
      scheduler: ScheduledExecutorService,
      applicationBuild: String,
    ): AnalyticsPublisher {
      synchronized(gate) {
        if (AnalyticsSettings.optedIn && !AnalyticsSettings.debugDisablePublishing) {
          anonymousInstance_ =
            GoogleAnalyticsPublisher(
              scheduler,
              Paths.get(AnalyticsPaths.spoolDirectory),
              applicationBuild,
            )
        } else {
          anonymousInstance_ = NullAnalyticsPublisher()
        }
        return anonymousInstance_
      }
    }

    /** Retrieved the configured publisher based on a call to [.initialize] */
    @JvmStatic
    val instance: AnalyticsPublisher
      get() =
        synchronized(gate) {
          return anonymousInstance_
        }

    /** Closes the current publisher and creates a new instance. */
    @JvmStatic
    fun updatePublisher(
      logger: ILogger,
      scheduler: ScheduledExecutorService,
      applicationBuild: String,
    ) {
      AnalyticsSettings.initialize(logger, scheduler)
      val current = instance
      try {
        current.close()
      } catch (e: Exception) {
        logger.error(e, "Unable to close existing analytics publisher")
      }

      initialize(scheduler, applicationBuild)
    }

    /**
     * Sets the instance of the publisher that sends messages with authorized headers
     *
     * @param logger used for logging errors
     * @param scheduler used to schedule periodic checks for data in the spool file using the
     *   publishing interval set on the publisher
     * @param applicationBuild application build used on the loq request
     * @param storeLocationId A unique id derived from the user's email address. Used to create the
     *   spool directory for that address
     * @param credentialsCallback callback to retrieve the authorization credentials
     */
    @JvmStatic
    fun setLoggedInPublisher(
      logger: ILogger,
      scheduler: ScheduledExecutorService,
      applicationBuild: String,
      storeLocationId: String,
      credentialsCallback: () -> String?,
    ) {
      updateLoggedInPublisher(
        GoogleAnalyticsPublisher(
          scheduler,
          Paths.get(AnalyticsPaths.spoolDirectory, storeLocationId),
          applicationBuild,
          credentialsCallback,
        ),
        logger,
      )
    }

    /**
     * Clears the instance of the publisher that sends messages with authorized headers
     *
     * @param logger used for logging errors
     */
    @JvmStatic
    fun clearLoggedInPublisher(logger: ILogger) {
      updateLoggedInPublisher(NullAnalyticsPublisher(), logger)
    }

    @JvmStatic
    private fun updateLoggedInPublisher(newPublisher: AnalyticsPublisher, logger: ILogger) {
      val oldLoginPublisher: AnalyticsPublisher
      synchronized(gate) {
        oldLoginPublisher = loginInstance_
        loginInstance_ = newPublisher
      }
      try {
        oldLoginPublisher.close()
      } catch (e: Exception) {
        logger.error(e, "Unable to close existing authorizing analytics publisher")
      }
    }

    @VisibleForTesting
    fun setAnonymousInstanceForTest(instance: AnalyticsPublisher) {
      this.anonymousInstance_ = instance
    }
  }
}
