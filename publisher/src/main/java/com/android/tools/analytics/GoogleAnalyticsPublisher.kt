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
import com.android.utils.StdLogger
import com.google.common.io.CountingOutputStream
import com.google.wireless.android.play.playlog.proto.ClientAnalytics
import com.google.wireless.android.play.playlog.proto.ClientAnalytics.LogRequest.LogSource
import com.google.wireless.android.sdk.stats.AndroidStudioEvent
import com.google.wireless.android.sdk.stats.MetaMetrics
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

/**
 * Publish collected analytics to Google's servers. Uses the provided [ScheduleExecutorService] to periodically (10 mins by default), scan
 * the provided spool location for new .trk files. If it finds any it parses the .trk files and uploads the parsed events to Google's
 * servers along with additional metadata such as meta metrics, client info and timing information..
 */
abstract class GoogleAnalyticsPublisher
/**
 * Creates a new instance for publishing metrics
 *
 * @param scheduler used for scheduling periodic checks of the spool location.
 * @param spoolLocation location to look for .trk files to upload.
 * @param applicationBuild application build used on the log request
 * @param logSource the [LogSource] to use for Clearcut logging.
 * @param serverUrl the server URL for the connetion to use.
 */
internal constructor(
  private val scheduler: ScheduledExecutorService,
  private val spoolLocation: Path,
  applicationBuild: String,
  logSource: LogSource,
  private var serverUrl: URL,
) : AnalyticsPublisher() {

  private val baseLogRequest: ClientAnalytics.LogRequest =
    ClientAnalytics.LogRequest.newBuilder()
      .setClientInfo(
        ClientAnalytics.ClientInfo.newBuilder()
          .setClientType(ClientAnalytics.ClientInfo.ClientType.DESKTOP)
          .setDesktopClientInfo(
            ClientAnalytics.DesktopClientInfo.newBuilder()
              .setLoggingId(AnalyticsSettings.userId)
              .setApplicationBuild(applicationBuild)
              .setOs(CommonMetricsData.osName)
              .setOsMajorVersion(CommonMetricsData.majorOsVersion!!)
              .setOsFullVersion(Environment.instance.getSystemProperty(Environment.SystemProperty.OS_VERSION))
          )
      )
      // Set the log source for the Clearcut service. This will depend on whether we are attaching
      // the authorization header.
      .setLogSource(logSource)
      .build()

  private var publishJob: ScheduledFuture<*>? = null
  private var scheduleVersion = 0
  private var bytesSentInLastPublish: Long = 0
  private var failedConnections = 0
  private var failedServerReplies = 0
  private var backoffRatio = 1
  private var createConnection_ = Callable<HttpURLConnection> { this.defaultCreateConnection() }
  private var logger: ILogger = StdLogger(StdLogger.Level.WARNING)

  init {
    // Schedule the first publish of logs from the spool directory.
    schedulePublish(publishInterval)
  }

  @Throws(Exception::class)
  override fun close() {
    synchronized(gate) {
      scheduleVersion++
      publishJob!!.cancel(false)
    }
  }

  override fun setPublishInterval(interval: Long, unit: TimeUnit) {
    synchronized(gate) {
      super.setPublishInterval(interval, unit)
      schedulePublish(publishInterval)
    }
  }

  /** Immediately uploads any queued .trk files to Google's servers, blocking until done. */
  override fun publishNow() {
    publishQueuedAnalytics()
  }

  /** Looks for any .trk files queued up in the spool directory and if so tries to publish them to Google's servers. */
  private fun publishQueuedAnalytics() {
    try {
      Files.newDirectoryStream(spoolLocation, "*.trk").use { stream ->
        for (file in stream) {
          // TODO: consider maintaining a list of failed .trk files and skip them after n
          // failures.
          // if publishing any file fails, we stop processing for this cycle and try again
          // in the next cycle.
          if (!tryPublishAnalytics(file)) {
            return
          }
        }
      }
    } catch (e: Exception) {
      logger.error(e, "Failure reading analytics spool directory.")
    }
  }

  protected open fun getCredentials(): String? = null

  protected open val credentialsRequired = false

  /**
   * Tries to publish analytics for the specified track file.
   *
   * @return true if file was uploaded successfully, skipped as it was locked or had zero events, false if uploading failed (connection or
   *   server error).
   */
  private fun tryPublishAnalytics(trackFile: Path): Boolean {
    val file = trackFile.toFile()
    var success = false
    try {
      RandomAccessFile(file, "rw").channel.use { channel ->
        channel.tryLock().use { lock ->
          if (lock == null) {
            // Another process has the file open (e.g. a command-line tool writing analytics).
            // skip for now but continue publishing other track files.
            return true
          }

          val entries = ArrayList<ClientAnalytics.LogEvent>()
          // Try to lock the file, this ensures no other code (e.g. the usage tracker)
          // has a lock on the file.
          val inputStream = Channels.newInputStream(channel)
          // read all LogEvents from the trackFile.
          while (true) {
            val event = ClientAnalytics.LogEvent.parseDelimitedFrom(inputStream) ?: break
            entries.add(event)
          }

          if (entries.isEmpty()) {
            // if this is an empty file, no need to publish, just delete the file and continue.
            success = true
          } else {
            // Add the meta metric log and build a LogRequest.
            val now = AnalyticsSettings.dateProvider.now().time
            entries.add(0, getMetaMetric(now))
            val request = buildLogRequest(entries, now)

            val credentials =
              if (credentialsRequired) {
                getCredentials() ?: return false
              } else {
                null
              }

            // Send the analytics to the specified server.
            val responseCode = trySendToServer(request, credentials)
            success = isSuccess(responseCode)
            if (success) {
              // only if publishing succeeded, delete the file, otherwise we'll try again.
              // successful publishing means we do no longer need to backoff.
              backoffRatio = 1
              failedConnections = 0
              failedServerReplies = 0
            } else {
              // publishing failed with a server error, track and increase our backoff ratio.
              failedServerReplies++
              backoffRatio *= 2
            }
          }
        }
      }
    } catch (e: IOException) {
      logger.error(e, "Failure publishing analytics, unable to connect to server")
      // publishing failed with a network error, track and increase our backoff ratio.
      failedConnections++
      backoffRatio *= 2
      // stop this publishing cycle, try again later.
      return false
    } catch (e: OverlappingFileLockException) {
      // Current process has the file open (e.g. JournalingUsageTracker).
      // skip for now but continue publishing other track files.
      return true
    }

    // We need to delete the file outside of the lock as deleting inside the lock doesn't
    // work on Windows.
    if (success) {
      file.delete()
    }
    return success
  }

  /** Default value of createConnection_. Uses current url to create a connection. */
  @Throws(IOException::class)
  private fun defaultCreateConnection(): HttpURLConnection? {
    val connection = serverUrl.openConnection()
    if (connection is HttpURLConnection) {
      return connection
    } else {
      logger.error(null, "Unexpected connection type %s", connection.javaClass.name)
      return null
    }
  }

  /**
   * Allows hosts of the publisher to plug in custom connections. E.g. to configure the connection to go over a proxy server specified in
   * the host of the publisher.
   */
  fun setCreateConnection(createConnection: Callable<HttpURLConnection>) {
    createConnection_ = createConnection
  }

  /**
   * Tries to upload metrics to the specified server using HTTP Post.
   *
   * @return http status code from the request.
   */
  @Throws(IOException::class)
  private fun trySendToServer(request: ClientAnalytics.LogRequest, credentials: String?): Int {
    val connection =
      try {
        createConnection_.call()
      } catch (e: Exception) {
        throw RuntimeException(e)
      }
        ?: // 'Method not allowed' as we don't have a valid connection.
        return 405

    connection.requestMethod = "POST"
    connection.doOutput = true

    // GZip the content to save bandwidth.
    connection.setRequestProperty("Content-Encoding", "gzip")

    // Set the authorization header and content type if the credentials are specified
    credentials?.let {
      connection.setRequestProperty("Authorization", "Bearer $credentials")
      connection.setRequestProperty("Content-Type", "application/octet-stream")
    }

    val requestBytes = request.toByteArray()
    connection.outputStream.use { output ->
      BufferedOutputStream(output).use { buffered ->
        CountingOutputStream(buffered).use { counted ->
          GZIPOutputStream(counted, true).use { zipped -> zipped.write(requestBytes) }
          bytesSentInLastPublish = counted.count
        }
      }
    }

    connection.connect()

    // Use headers from result to update our dateProvider to avoid clock skew (e.g. from the user
    // changing the clock).
    if (AnalyticsSettings.googlePlayDateProvider != null) {
      AnalyticsSettings.googlePlayDateProvider!!.updateServerTimestampFromExistingConnection(connection)
    }

    val responseCode = connection.responseCode
    if (!isSuccess(responseCode)) {
      logger.error(
        null,
        "Failure publishing metrics. Server responded with status code '%d' and message '%s'",
        responseCode,
        connection.responseMessage,
      )
    }
    return responseCode
  }

  /** Builds a [ClientAnalytics.LogRequest] proto based on the provided entries and time. */
  private fun buildLogRequest(entries: List<ClientAnalytics.LogEvent>, time: Long): ClientAnalytics.LogRequest {
    return ClientAnalytics.LogRequest.newBuilder(baseLogRequest)
      .apply { clientInfoBuilder.desktopClientInfoBuilder.setLoggingId(AnalyticsSettings.userId) }
      .setRequestTimeMs(time)
      .addAllLogEvent(entries)
      .build()
  }

  /** Creates [ClientAnalytics.LogEvent] with meta metrics, used to measure the health of our metrics reporting system. */
  private fun getMetaMetric(time: Long): ClientAnalytics.LogEvent {
    return ClientAnalytics.LogEvent.newBuilder()
      .setEventTimeMs(time)
      .setSourceExtension(
        AndroidStudioEvent.newBuilder()
          .setCategory(AndroidStudioEvent.EventCategory.META)
          .setKind(AndroidStudioEvent.EventKind.META_METRICS)
          .setMetaMetrics(
            MetaMetrics.newBuilder()
              .setBytesSentInLastUpload(bytesSentInLastPublish)
              .setFailedConnections(failedConnections)
              .setFailedServerReplies(failedServerReplies)
          )
          .build()
          .toByteString()
      )
      .build()
  }

  /**
   * Schedules the job that looks for .trk files and publishes them to Google's servers. Needs to be called while locked on [.gate]. NOTE:
   * this method is self-rescheduling.
   */
  private fun schedulePublish(publishIntervalNanoSeconds: Long) {
    val currentScheduleVersion = ++scheduleVersion

    // if any existing publish is pending and it hasn't started yet, cancel it as we've been
    // provided a new interval to schedule on and it would be odd if the a job still got run
    // at the old schedule.
    publishJob?.cancel(false)
    publishJob =
      scheduler.schedule(
        {
          synchronized(gate) {
            publishQueuedAnalytics()
            // only schedule next beat if we're still the authority.
            if (scheduleVersion == currentScheduleVersion) {
              schedulePublish(publishIntervalNanoSeconds)
            }
          }
        },
        // Next job is scheduled with exponential backoff with a max of 1 day.
        // this is reset to 1 when the job successfully completes.
        Math.min(publishIntervalNanoSeconds * backoffRatio, TimeUnit.DAYS.toNanos(1)),
        TimeUnit.NANOSECONDS,
      )
  }

  /** Gets the address of the server currently used to publish to. */
  fun getServerUrl(): URL {
    return serverUrl
  }

  /** Updates the server used to publish analytics to. */
  fun setServerUrl(serverUrl: URL): GoogleAnalyticsPublisher {
    synchronized(gate) { this.serverUrl = serverUrl }
    return this
  }

  companion object {

    // While access to fields is atomic (due to volatile on long & doubles), this code has many
    // methods that operate on various variables at once. We synchronize any method that operates
    // on multiple variables or access members of those variables (e.g. method calls).
    private val gate = Any()

    /** Checks if the http status code indicates success or not. */
    private fun isSuccess(statusCode: Int): Boolean {
      return statusCode in 200..299
    }
  }
}
