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
import java.nio.file.Paths
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.jetbrains.annotations.TestOnly

/**
 * Base class for publishing analytics. This class has two subclasses, one that publishes analytics to Google's servers for users who opted
 * in to metrics and one that is a Noop to ensure metrics never get published for users who opt out.
 */
abstract class AnalyticsPublisher protected constructor() : AutoCloseable {

  /** Gets the interval in nanoseconds used for scheduling jobs to publish metrics. */
  var publishInterval = TimeUnit.MINUTES.toNanos(10)
    private set

  /** Sets the interval used for scheduling jobs to publish metrics. */
  open fun setPublishInterval(interval: Long, unit: TimeUnit) {
    publishInterval = unit.toNanos(interval)
  }

  /**
   * Immediately scans the spool directory and uploads any queued analytics to Google's servers. Blocks until the upload attempt completes.
   * Used by the `upload-metrics` subcommand to perform a synchronous flush rather than waiting for the next scheduled publishing window.
   */
  abstract fun publishNow()

  companion object {

    private var anonymousInstance_: AnalyticsPublisher = NullAnalyticsPublisher
    private var loggedInInstance_: AnalyticsPublisher = NullAnalyticsPublisher
    private lateinit var applicationBuild: String
    private lateinit var scheduler: ScheduledExecutorService
    private lateinit var logger: ILogger
    private var initialized = false
    private val gate = Any()

    /**
     * Initializes the publisher retrieved by [.getInstance]
     *
     * @param scheduler used to schedule jobs for publishing.
     * @param applicationBuild version information about the app publishing analytics.
     */
    @JvmStatic
    fun initialize(logger: ILogger, scheduler: ScheduledExecutorService, applicationBuild: String): AnalyticsPublisher {
      synchronized(gate) {
        AnalyticsPublisher.logger = logger
        AnalyticsPublisher.scheduler = scheduler
        AnalyticsPublisher.applicationBuild = applicationBuild
        anonymousInstance_ =
          if (AnalyticsSettings.optedIn && !AnalyticsSettings.debugDisablePublishing) {
            GoogleAnalyticsPublisher(scheduler, Paths.get(AnalyticsPaths.spoolDirectory), applicationBuild)
          } else {
            NullAnalyticsPublisher
          }
        initialized = true
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
    fun updatePublisher(logger: ILogger, scheduler: ScheduledExecutorService, applicationBuild: String) {
      AnalyticsSettings.initialize(logger, scheduler)
      val current = instance
      try {
        current.close()
      } catch (e: Exception) {
        logger.error(e, "Unable to close existing analytics publisher")
      }

      initialize(logger, scheduler, applicationBuild)
    }

    /**
     * Sets the instance of the publisher that sends messages with authorized headers
     *
     * @param storeLocationId A unique id derived from the user's email address. Used to create the spool directory for that address
     * @param credentialsCallback callback to retrieve the authorization credentials
     */
    @JvmStatic
    fun setLoggedInPublisher(storeLocationId: String, credentialsCallback: () -> String?) {
      synchronized(gate) {
        // This indicates that the anonymous publisher has not been set yet. It is required to be set
        // first so that these three properties are set.
        if (!initialized) {
          throw RuntimeException("call to setLoggedInPublisher before initialization")
        }
      }

      updateLoggedInPublisher(
        GoogleAnalyticsPublisher(
          scheduler,
          Paths.get(AnalyticsPaths.spoolDirectory, storeLocationId),
          applicationBuild,
          credentialsCallback,
        )
      )
    }

    /** Clears the instance of the publisher that sends messages with authorized headers */
    @JvmStatic
    fun clearLoggedInPublisher() {
      updateLoggedInPublisher(NullAnalyticsPublisher)
    }

    @TestOnly
    @JvmStatic
    fun getLoggedInInstanceForTest(): AnalyticsPublisher {
      return loggedInInstance_
    }

    @JvmStatic
    private fun updateLoggedInPublisher(newPublisher: AnalyticsPublisher) {
      val oldLoggedInPublisher: AnalyticsPublisher
      synchronized(gate) {
        oldLoggedInPublisher = loggedInInstance_
        loggedInInstance_ = newPublisher
      }
      try {
        oldLoggedInPublisher.close()
      } catch (e: Exception) {
        logger.error(e, "Unable to close existing authorizing analytics publisher")
      }
    }

    @TestOnly
    fun setAnonymousInstanceForTest(instance: AnalyticsPublisher) {
      this.anonymousInstance_ = instance
    }
  }
}
