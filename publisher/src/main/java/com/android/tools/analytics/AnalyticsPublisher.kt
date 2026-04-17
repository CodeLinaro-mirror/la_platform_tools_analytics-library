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
import com.google.common.hash.Hashing
import java.nio.file.Paths
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.annotations.TestOnly

/**
 * Base class for publishing analytics. This class has two subclasses, one that publishes analytics to Google's servers for users who opted
 * in to metrics and one that is a Noop to ensure metrics never get published for users who opt out.
 */
abstract class AnalyticsPublisher protected constructor() : AutoCloseable {
  private data class Publishers(val anonymous: AnalyticsPublisher, val loggedIn: AnalyticsPublisher) : AutoCloseable {
    fun publish() {
      try {
        anonymous.publishNow()
      } catch (e: Exception) {
        logger.error(e, "Unable to publish anonymous analytics")
      }

      try {
        loggedIn.publishNow()
      } catch (e: Exception) {
        logger.error(e, "Unable to publish logged-in analytics")
      }
    }

    override fun close() {
      try {
        anonymous.close()
      } catch (e: Exception) {
        logger.error(e, "Unable to close existing anonymous analytics publisher")
      }

      try {
        loggedIn.close()
      } catch (e: Exception) {
        logger.error(e, "Unable to close existing logged-in analytics publisher")
      }
    }
  }

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

  @TestOnly open fun isScheduled() = false

  companion object {
    @VisibleForTesting var anonymousInstance: AnalyticsPublisher = NullAnalyticsPublisher
    @VisibleForTesting var loggedInInstance: AnalyticsPublisher = NullAnalyticsPublisher

    private lateinit var applicationBuild: String
    private lateinit var scheduler: ScheduledExecutorService
    private lateinit var logger: ILogger

    private val gate = Any()
    private var job: Job? = null

    /**
     * Initializes the publisher retrieved by [.getInstance]
     *
     * @param scheduler used to schedule jobs for publishing.
     * @param applicationBuild version information about the app publishing analytics.
     */
    @JvmStatic
    fun initialize(logger: ILogger, scheduler: ScheduledExecutorService, applicationBuild: String): AnalyticsPublisher {
      AnalyticsSettings.initialize(logger, scheduler)
      synchronized(gate) {
        if (job != null) {
          throw RuntimeException("initialize called more than once")
        }

        AnalyticsPublisher.logger = logger
        AnalyticsPublisher.scheduler = scheduler
        AnalyticsPublisher.applicationBuild = applicationBuild

        // setPublishers is called so that the instances are set by the time initialize returns.
        // This is required by some callers such as Lint.
        setPublishers(AnalyticsStateManager.analyticsStateFlow.value)

        val scope = CoroutineScope(scheduler.asCoroutineDispatcher())
        job = AnalyticsStateManager.analyticsStateFlow.onEach { stateChanged(it) }.launchIn(scope)

        return anonymousInstance
      }
    }

    /**
     * updatePublisher is only present to maintain backwards compatibility with Sherlock. All other clients should use initialize(...)
     * instead.
     *
     * TODO(b/438541344): remove this once Sherlock has been updated
     */
    @JvmStatic
    fun updatePublisher(logger: ILogger, scheduler: ScheduledExecutorService, applicationBuild: String) {
      AnalyticsSettings.initialize(logger, scheduler)
      val oldPublishers: Publishers

      synchronized(gate) {
        AnalyticsPublisher.logger = logger
        AnalyticsPublisher.scheduler = scheduler
        AnalyticsPublisher.applicationBuild = applicationBuild

        val level =
          if (AnalyticsSettings.optedIn && !AnalyticsSettings.debugDisablePublishing) {
            AnalyticsLevel.ANONYMOUS
          } else {
            AnalyticsLevel.NONE
          }

        oldPublishers = getPublishers()
        setPublishers(AnalyticsState(level, null))
      }

      oldPublishers.close()
    }

    private fun stateChanged(state: AnalyticsState) {
      if (state.level == AnalyticsLevel.LOGGED_IN) {
        require(state.loggedInUser != null) { "A user is required to enable logged in metrics." }
      }

      val oldPublishers: Publishers

      synchronized(gate) {
        oldPublishers = getPublishers()
        setPublishers(state)
      }

      oldPublishers.close()
    }

    @TestOnly
    fun updateState() {
      stateChanged(AnalyticsStateManager.analyticsStateFlow.value)
    }

    /** Immediately uploads any queued .trk files to Google's servers, blocking until done. */
    @JvmStatic
    fun publish() {
      val publishers: Publishers

      synchronized(gate) { publishers = getPublishers() }

      publishers.publish()
    }

    @TestOnly
    fun reset() {
      val oldPublishers: Publishers
      val oldJob: Job?

      synchronized(gate) {
        oldJob = job
        job = null

        oldPublishers = getPublishers()
        stateChanged(AnalyticsState(AnalyticsLevel.NONE, null))
      }

      oldJob?.cancel()
      oldPublishers.close()
    }

    private fun getPublishers(): Publishers {
      return Publishers(anonymousInstance, loggedInInstance)
    }

    private fun setPublishers(state: AnalyticsState) {
      anonymousInstance = createAnonymousPublisher(state.level)
      loggedInInstance = createLoggedInPublisher(state)
    }

    private fun createAnonymousPublisher(level: AnalyticsLevel): AnalyticsPublisher {
      return if (level == AnalyticsLevel.NONE || AnalyticsSettings.debugDisablePublishing) {
        NullAnalyticsPublisher
      } else {
        val path = Paths.get(AnalyticsPaths.spoolDirectory)
        AnonymousAnalyticsPublisher(scheduler, path, applicationBuild)
      }
    }

    private fun createLoggedInPublisher(state: AnalyticsState): AnalyticsPublisher {
      val level = state.level
      val loggedInUser = state.loggedInUser

      return if (level == AnalyticsLevel.LOGGED_IN && loggedInUser != null && !AnalyticsSettings.debugDisablePublishing) {
        val spoolLocationId = Hashing.farmHashFingerprint64().hashUnencodedChars(loggedInUser.emailAddress.lowercase()).toString()
        val path = Paths.get(AnalyticsPaths.spoolDirectory, spoolLocationId)
        LoggedInAnalyticsPublisher(scheduler, path, applicationBuild, loggedInUser.callback)
      } else {
        NullAnalyticsPublisher
      }
    }
  }
}
