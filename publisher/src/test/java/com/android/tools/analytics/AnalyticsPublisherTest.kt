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

import com.android.testutils.SystemPropertyOverrides
import com.android.testutils.VirtualTimeDateProvider
import com.android.testutils.VirtualTimeScheduler
import com.android.utils.DateProvider
import com.android.utils.StdLogger
import com.google.wireless.android.play.playlog.proto.ClientAnalytics
import com.google.wireless.android.sdk.stats.AndroidStudioEvent
import com.google.wireless.android.sdk.stats.MetaMetrics
import com.google.wireless.android.sdk.stats.StudioCrash
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Tests for [AnalyticsPublisher] and [GoogleAnalyticsPublisher]. */
class AnalyticsPublisherTest {

  @get:Rule val testSpoolDir = TemporaryFolder()
  @get:Rule val testConfigDir = TemporaryFolder()

  @Before
  fun before() {
    val analyticsSettings = AnalyticsSettingsData()
    analyticsSettings.optedIn = true
    analyticsSettings.userId = "f59e9566-2416-42a9-a159-b91fa484e4d7"
    AnalyticsSettings.setInstanceForTest(analyticsSettings)
  }

  @Test
  @Throws(Exception::class)
  fun testInitialValues() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    // Start a stub webserver to publish to.
    try {
      ServerStub().use { stub ->
        // Create helpers used to instantiate the publisher.
        val vs = VirtualTimeScheduler()

        // Instantiate the publisher
        val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")
        googleAnalyticsPublisher.setServerUrl(stub.url)

        // Ensure the publisher's initial values are as expected.
        assertEquals(stub.url, googleAnalyticsPublisher.getServerUrl())
        assertEquals(TimeUnit.MINUTES.toNanos(10), googleAnalyticsPublisher.publishInterval)

        // Ensure that the first publish job has been scheduled.
        assertEquals(1, vs.queue.size.toLong())
        assertEquals(TimeUnit.MINUTES.toNanos(10), vs.queue.peek().tick)
      }
    } finally {
      cleanEnvironment()
    }
  }

  private fun cleanEnvironment() {
    EnvironmentFakes.setSystemEnvironment()
  }

  @Test
  @Throws(Exception::class)
  fun testBasics() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      ServerStub().use { stub ->
        SystemPropertyOverrides().use { systemPropertyOverrides ->

          // Create an event to log.
          val logged = createAndroidStudioEvent(5)

          // Use the JournalingUsageTracker to place some .trk files with events in the spool
          // directory.
          val vs = VirtualTimeScheduler()
          val journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())
          journalingUsageTracker.logNow(logged)
          vs.advanceBy(0)
          journalingUsageTracker.close()

          // Override the date provider to the publisher so we can reliably check if date based
          // properties are set correctly.
          val dateProvider = VirtualTimeDateProvider(vs)
          AnalyticsSettings.dateProvider = dateProvider

          // move the scheduler ahead so we get non zero values for the date provider.
          vs.advanceBy(1, TimeUnit.MINUTES)

          // override the os.* system properties so the test runs reliably no matter which
          // it is run on.
          systemPropertyOverrides.setProperty("os.name", "Linux")
          systemPropertyOverrides.setProperty("os.version", "3.13.0-85-generic")

          val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")
          googleAnalyticsPublisher.setServerUrl(stub.url)

          // advance time to make the publisher run its first publishing job.
          vs.advanceBy(10, TimeUnit.MINUTES)
          googleAnalyticsPublisher.close()

          // retrieve results from the webserver stub.
          val results = stub.results
          assertEquals(1, results.size.toLong())
          val result = results[0]
          assertEquals(true, result.isDone)
          val request = result.get().logRequest

          // verify the retrieved proto is shaped as expected.
          assertEquals(660000, request.requestTimeMs)

          assertEquals(ClientAnalytics.LogRequest.LogSource.ANDROID_STUDIO, request.logSource)
          assertEquals(ClientAnalytics.ClientInfo.ClientType.DESKTOP, request.clientInfo.clientType)
          val cdi = request.clientInfo.desktopClientInfo
          assertEquals(AnalyticsSettings.userId, cdi.loggingId)
          assertEquals("linux", cdi.os)
          assertEquals("3.13", cdi.osMajorVersion)
          assertEquals("3.13.0-85-generic", cdi.osFullVersion)
          assertEquals("1.2.3.4", cdi.applicationBuild)

          assertEquals(2, request.logEventCount.toLong())
          val metaEvent = request.getLogEvent(0)
          val metaStudioEvent = AndroidStudioEvent.parseFrom(metaEvent.sourceExtension)
          assertEquals(
            AndroidStudioEvent.newBuilder()
              .setCategory(AndroidStudioEvent.EventCategory.META)
              .setKind(AndroidStudioEvent.EventKind.META_METRICS)
              .setMetaMetrics(
                MetaMetrics.newBuilder().setFailedConnections(0).setFailedServerReplies(0).setBytesSentInLastUpload(0).build()
              )
              .build(),
            metaStudioEvent,
          )

          val userEvent = request.getLogEvent(1)
          val retrieved = AndroidStudioEvent.parseFrom(userEvent.sourceExtension)
          assertEquals(logged.build(), retrieved)
        }
      }
    } finally {
      AnalyticsSettings.dateProvider = DateProvider.SYSTEM
      cleanEnvironment()
    }
    // ensure the spool directory is empty after successfully publishing the analytics.
    assertEquals(0, testSpoolDir.root.listFiles()!!.size.toLong())
  }

  @Test
  @Throws(Exception::class)
  fun testCredentials() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    val credentials = "credentials"
    try {
      ServerStub().use { stub ->
        SystemPropertyOverrides().use { systemPropertyOverrides ->

          // Create an event to log.
          val logged = createAndroidStudioEvent(5)

          // Use the JournalingUsageTracker to place some .trk files with events in the spool
          // directory.
          val vs = VirtualTimeScheduler()
          val journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())
          journalingUsageTracker.logNow(logged)
          vs.advanceBy(0)
          journalingUsageTracker.close()

          // Override the date provider to the publisher so we can reliably check if date based
          // properties are set correctly.
          val dateProvider = VirtualTimeDateProvider(vs)
          AnalyticsSettings.dateProvider = dateProvider

          // move the scheduler ahead so we get non zero values for the date provider.
          vs.advanceBy(1, TimeUnit.MINUTES)

          // override the os.* system properties so the test runs reliably no matter which
          // it is run on.
          systemPropertyOverrides.setProperty("os.name", "Linux")
          systemPropertyOverrides.setProperty("os.version", "3.13.0-85-generic")

          val googleAnalyticsPublisher = LoggedInAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4") { credentials }
          googleAnalyticsPublisher.setServerUrl(stub.url)

          // advance time to make the publisher run its first publishing job.
          vs.advanceBy(10, TimeUnit.MINUTES)
          googleAnalyticsPublisher.close()

          // retrieve results from the webserver stub.
          val results = stub.results
          assertEquals(1, results.size.toLong())
          val result = results[0]
          assertEquals(true, result.isDone)

          val request = result.get().logRequest
          assertEquals(ClientAnalytics.LogRequest.LogSource.ANDROID_STUDIO_EVENT_LOGGED_IN, request.logSource)

          val headers = result.get().authorizationHeaders
          assertNotNull(headers)
          headers?.let {
            assertEquals(1, headers.count())
            assertEquals("Bearer $credentials", headers.firstOrNull())
          }
        }
      }
    } finally {
      AnalyticsSettings.dateProvider = DateProvider.SYSTEM
      cleanEnvironment()
    }
    // ensure the spool directory is empty after successfully publishing the analytics.
    assertEquals(0, testSpoolDir.root.listFiles()!!.size.toLong())
  }

  /** Helper that builds a [AndroidStudioEvent] with a marker to distinguish this message. */
  private fun createAndroidStudioEvent(marker: Long): AndroidStudioEvent.Builder {
    return AndroidStudioEvent.newBuilder()
      .setStudioSessionId(UsageTracker.sessionId)
      .setIdeBrand(AndroidStudioEvent.IdeBrand.UNKNOWN_IDE_BRAND)
      .setCategory(AndroidStudioEvent.EventCategory.PING)
      .setKind(AndroidStudioEvent.EventKind.STUDIO_PING)
      .setStudioCrash(StudioCrash.newBuilder().setActions(marker))
  }

  @Test
  @Throws(Exception::class)
  fun testBadConnection() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    // Create a server
    try {
      ServerStub().use { stub ->

        // Create an event to log.
        val logged = createAndroidStudioEvent(3)

        // Use the AnonymousUsageTrackerWriter to place some .trk files with events in the spool
        // directory.
        val vs = VirtualTimeScheduler()
        val journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())
        journalingUsageTracker.logNow(logged)
        vs.advanceBy(0)
        journalingUsageTracker.close()

        // Create helpers used to instantiate the publisher.
        val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")

        // set the url to publish to to a reserved port which we know the server cannot connect to.
        // https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.txt
        googleAnalyticsPublisher.setServerUrl(URL("http://localhost:270/"))

        // Execute the first publish job.
        vs.advanceBy(10, TimeUnit.MINUTES)
        // Ensure no files were published.
        assertEquals(1, testSpoolDir.root.listFiles()!!.size.toLong())
        // Ensure that the next job is scheduled at 20 mins
        // (2 * the normal time because of backoff).
        assertEquals(1, vs.queue.size.toLong())
        assertEquals(20, vs.queue.peek().getDelay(TimeUnit.MINUTES))

        // Configure the publisher to use our stub server instead.
        googleAnalyticsPublisher.setServerUrl(stub.url)
        // Move scheduler to run to the delayed job
        vs.advanceBy(20, TimeUnit.MINUTES)
        googleAnalyticsPublisher.close()

        // Ensure that the results do come in now.
        val results = stub.results
        assertEquals(1, results.size.toLong())
        val result = results[0]
        assertEquals(true, result.isDone)
        val request = result.get().logRequest

        assertEquals(2, request.logEventCount.toLong())
        val metaEvent = request.getLogEvent(0)
        val metaStudioEvent = AndroidStudioEvent.parseFrom(metaEvent.sourceExtension)
        assertEquals(
          AndroidStudioEvent.newBuilder()
            .setCategory(AndroidStudioEvent.EventCategory.META)
            .setKind(AndroidStudioEvent.EventKind.META_METRICS)
            .setMetaMetrics(
              MetaMetrics.newBuilder()
                // ensure that the previous failure is reported in the
                // meta metrics.
                .setFailedConnections(1)
                .setFailedServerReplies(0)
                .setBytesSentInLastUpload(0)
                .build()
            )
            .build(),
          metaStudioEvent,
        )
      }
    } finally {
      cleanEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun testBadServer() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      ServerStub().use { stub ->
        SystemPropertyOverrides().use { systemPropertyOverrides ->

          // Create an event to log.
          val logged = createAndroidStudioEvent(3)

          // Use the AnonymousUsageTrackerWriter to place some .trk files with events in the spool
          // directory.
          val vs = VirtualTimeScheduler()

          // hardcode session id to ensure stable compression sizes.
          UsageTracker.sessionId = "2a2a42e3-80b4-418f-8e19-21af36146a58"

          // As we're checking upload byte sizes and the size varies by the value for
          // time, we need to fix the time in this test.
          val dateProvider = VirtualTimeDateProvider(vs)
          AnalyticsSettings.dateProvider = dateProvider

          var journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())

          journalingUsageTracker.logNow(logged)
          vs.advanceBy(0)
          journalingUsageTracker.close()

          // override the os.* system properties so the test runs reliably no matter which
          // it is run on.
          systemPropertyOverrides.setProperty("os.name", "Linux")
          systemPropertyOverrides.setProperty("os.version", "3.13.0-85-generic")

          val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")
          googleAnalyticsPublisher.setServerUrl(stub.url)
          googleAnalyticsPublisher.setCreateConnection(
            Callable {
              val connection = stub.url.openConnection() as HttpURLConnection
              connection.connectTimeout = 10000
              connection.readTimeout = 10000
              connection
            }
          )

          // Instruct to make the server stub fail the http request in the next call.
          stub.makeNextResponseServerError(true)

          // Execute the first publish job.
          vs.advanceBy(10, TimeUnit.MINUTES)

          // Ensure no files were published.
          assertEquals(1, testSpoolDir.root.listFiles()!!.size.toLong())

          // Ensure that the next job is scheduled at 20 mins
          // (2 * the normal time because of backoff).
          assertEquals(1, vs.queue.size.toLong())
          assertEquals(20, vs.queue.peek().getDelay(TimeUnit.MINUTES))

          // Move scheduler to run to the delayed job
          vs.advanceBy(20, TimeUnit.MINUTES)

          // Ensure that the results do come in now.
          var results: List<Future<PublishResult>> = stub.results
          assertEquals(1, results.size.toLong())
          var result: Future<PublishResult> = results[0]
          assertEquals(true, result.isDone)
          var request: ClientAnalytics.LogRequest = result.get().logRequest

          assertEquals(2, request.logEventCount.toLong())
          var metaEvent: ClientAnalytics.LogEvent = request.getLogEvent(0)
          var metaStudioEvent = AndroidStudioEvent.parseFrom(metaEvent.sourceExtension)

          var bytesSentInLastUpload = metaStudioEvent.metaMetrics.bytesSentInLastUpload
          assertTrue("bytes_sent_in_last_upload should be > 0", bytesSentInLastUpload > 0)

          assertEquals(
            AndroidStudioEvent.newBuilder()
              .setCategory(AndroidStudioEvent.EventCategory.META)
              .setKind(AndroidStudioEvent.EventKind.META_METRICS)
              .setMetaMetrics(
                MetaMetrics.newBuilder()
                  .setFailedConnections(0)
                  // ensure that the previous failure is reported in the
                  // meta metrics.
                  .setFailedServerReplies(1)
                  .setBytesSentInLastUpload(bytesSentInLastUpload)
                  .build()
              )
              .build(),
            metaStudioEvent,
          )

          googleAnalyticsPublisher.close()
        }
      }
    } finally {
      AnalyticsSettings.dateProvider = DateProvider.SYSTEM
      cleanEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun testEmptySpoolFile() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      ServerStub().use { stub ->
        // Use the AnonymousUsageTrackerWriter to place an empty .trk file in the spool directory.
        val vs = VirtualTimeScheduler()
        val journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())
        journalingUsageTracker.close()

        // Create helpers used to instantiate the publisher.
        val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")
        googleAnalyticsPublisher.setServerUrl(stub.url)

        // Execute the first publish job.
        vs.advanceBy(10, TimeUnit.MINUTES)

        // Ensure the .trk file got removed.
        assertEquals(0, testSpoolDir.root.listFiles()!!.size.toLong())
        googleAnalyticsPublisher.close()

        // Ensure no events were published.
        val results = stub.results
        assertEquals(0, results.size.toLong())
      }
    } finally {
      cleanEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun testMultipleEvents() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      // Create a few events to log.
      val logged1 = createAndroidStudioEvent(1)
      val logged2 = createAndroidStudioEvent(2)
      val logged3 = createAndroidStudioEvent(3)
      val logged4 = createAndroidStudioEvent(4)

      val expected = HashSet<AndroidStudioEvent>()
      expected.add(logged1.build())
      expected.add(logged2.build())
      expected.add(logged3.build())
      expected.add(logged4.build())

      // Use the AnonymousUsageTrackerWriter to place several .trk files with events in the spool
      // directory.
      val vs = VirtualTimeScheduler()
      val journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())
      UsageTracker.maxJournalSize = 2
      journalingUsageTracker.logNow(logged1)
      journalingUsageTracker.logNow(logged2)
      vs.advanceBy(0)
      journalingUsageTracker.logNow(logged3)
      journalingUsageTracker.logNow(logged4)
      vs.advanceBy(0)
      journalingUsageTracker.close()

      // Create helpers used to instantiate the publisher.
      ServerStub().use { stub ->
        val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")
        googleAnalyticsPublisher.setServerUrl(stub.url)

        // Execute the first publish job.
        vs.advanceBy(10, TimeUnit.MINUTES)
        googleAnalyticsPublisher.close()

        // check that two requests were made
        val results = stub.results
        assertEquals(2, results.size.toLong())

        val actual = HashSet<AndroidStudioEvent>()

        // each request contains 3 events (1 meta and two data events).
        val result1 = results[0]
        val request1 = result1.get().logRequest
        assertEquals(3, request1.logEventCount.toLong())
        val received1 = AndroidStudioEvent.parseFrom(request1.getLogEvent(1).sourceExtension)
        actual.add(received1)
        val received2 = AndroidStudioEvent.parseFrom(request1.getLogEvent(2).sourceExtension)
        actual.add(received2)

        val result2 = results[1]
        val request2 = result2.get().logRequest
        assertEquals(3, request2.logEventCount.toLong())
        val received3 = AndroidStudioEvent.parseFrom(request2.getLogEvent(1).sourceExtension)
        actual.add(received3)
        val received4 = AndroidStudioEvent.parseFrom(request2.getLogEvent(2).sourceExtension)
        actual.add(received4)

        // ensure all events that were sent are received, but don't care about the order.
        assertEquals(expected, actual)
      }
    } finally {
      cleanEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun testUpdateInterval() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      ServerStub().use { stub ->
        // Create an event to log.
        val logged = createAndroidStudioEvent(5)

        // Use the AnonymousUsageTrackerWriter to place some .trk files with events in the spool
        // directory.
        val vs = VirtualTimeScheduler()
        val journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())
        journalingUsageTracker.logNow(logged)
        vs.advanceBy(0)
        journalingUsageTracker.close()

        // Create helpers used to instantiate the publisher.
        val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")
        googleAnalyticsPublisher.setServerUrl(stub.url)

        // Ensure a job is queued to publish analytics.
        assertEquals(1, vs.queue.size.toLong())
        assertEquals(10, vs.queue.peek().getDelay(TimeUnit.MINUTES))

        // Move time but not enough to trigger the job
        vs.advanceBy(5, TimeUnit.MINUTES)
        assertEquals(1, vs.queue.size.toLong())
        assertEquals(5, vs.queue.peek().getDelay(TimeUnit.MINUTES))

        // Update the publish interval
        googleAnalyticsPublisher.setPublishInterval(12, TimeUnit.MINUTES)

        // Ensure that the publish job has been updated to match the new interval.
        assertEquals(1, vs.queue.size.toLong())
        assertEquals(12, vs.queue.peek().getDelay(TimeUnit.MINUTES))

        // Move time forward by new interval
        vs.advanceBy(12, TimeUnit.MINUTES)
        googleAnalyticsPublisher.close()

        // Ensure that analytics are published after the interval.
        val results = stub.results
        assertEquals(1, results.size.toLong())
      }
    } finally {
      cleanEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun testCustomConnection() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      ServerStub().use { stub ->
        // Create an event to log.
        val logged = createAndroidStudioEvent(5)

        // Use the AnonymousUsageTrackerWriter to place some .trk files with events in the spool
        // directory.
        val vs = VirtualTimeScheduler()
        val journalingUsageTracker = AnonymousUsageTrackerWriter(vs, testSpoolDir.root.toPath())
        journalingUsageTracker.logNow(logged)
        vs.advanceBy(0)
        journalingUsageTracker.close()

        // Create an instance of the publisher with a customized connection creation function.
        val googleAnalyticsPublisher = AnonymousAnalyticsPublisher(vs, testSpoolDir.root.toPath(), "1.2.3.4")
        googleAnalyticsPublisher.setCreateConnection(Callable { stub.url.openConnection() as HttpURLConnection })
        // set the url to publish to to a reserved port which we know the server cannot connect
        // to.
        // https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.txt
        googleAnalyticsPublisher.setServerUrl(URL("http://localhost:1023/bad"))

        // Move time forward to schedule the upload.
        vs.advanceBy(10, TimeUnit.MINUTES)
        googleAnalyticsPublisher.close()

        // Ensure that analytics are published after the interval.
        val results = stub.results
        assertEquals(1, results.size.toLong())
      }
    } finally {
      cleanEnvironment()
    }
  }

  @Test
  fun testUpdatePublisher() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      // Create helpers used to instantiate the publisher.
      val vs = VirtualTimeScheduler()

      // update the publisher, first call will initialize.
      AnalyticsPublisher.updatePublisher(StdLogger(StdLogger.Level.ERROR), vs, "1.2.3.4")
      val afterFirstUpdate = AnalyticsPublisher.anonymousInstance
      assertTrue(afterFirstUpdate is GoogleAnalyticsPublisher)

      // ensure a job is scheduled for the first publisher.
      val job = vs.queue.peek()
      assertNotNull(job)

      // update again, but now opt-ed out.
      AnalyticsSettings.optedIn = false
      AnalyticsPublisher.updatePublisher(StdLogger(StdLogger.Level.ERROR), vs, "1.2.3.4")
      val afterSecondUpdate = AnalyticsPublisher.anonymousInstance
      assertTrue(afterSecondUpdate is NullAnalyticsPublisher)

      // ensure job from first publisher has been canceled as part of update.
      assertTrue(job.isCancelled)
    } finally {
      cleanEnvironment()
    }
  }
}
