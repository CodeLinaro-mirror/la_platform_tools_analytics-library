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

import com.android.testutils.VirtualTimeDateProvider
import com.android.testutils.VirtualTimeScheduler
import com.android.utils.DateProvider
import com.android.utils.StdLogger
import com.google.protobuf.InvalidProtocolBufferException
import com.google.wireless.android.play.playlog.proto.ClientAnalytics
import com.google.wireless.android.sdk.stats.AndroidStudioEvent
import com.google.wireless.android.sdk.stats.MetaMetrics
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.Channels
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Tests for [JournalingUsageTracker].
 */
class JournalingUsageTrackerTest {
  @get:Rule
  var testConfigDir = TemporaryFolder()
  @get:Rule
  var testSpoolDir = TemporaryFolder()

  @Test
  @Throws(Exception::class)
  fun trackerBasicTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // Setup up an instance of the JournalingUsageTracker using a temp spool directory and
      //  virtual time scheduler.
      val virtualTimeScheduler = VirtualTimeScheduler()
      val journalingUsageTracker = JournalingUsageTracker(virtualTimeScheduler,
                                                          testSpoolDir.root.toPath())

      // Create a log entry and log it.
      val logEntry = createAndroidStudioEvent(42)
      journalingUsageTracker.logNow(logEntry)
      // Ensure this triggers an action on the scheduler and run the action
      assertEquals(1, virtualTimeScheduler.actionsQueued)
      virtualTimeScheduler.advanceBy(0)
      assertEquals(0, virtualTimeScheduler.actionsQueued)

      // The action should have written to the still locked spool file.
      val beforeClose = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, beforeClose.lockedFiles.size.toLong())
      assertEquals(0, beforeClose.completedLogs.size.toLong())

      // Close the usage tracker
      journalingUsageTracker.close()

      // Ensure that closing the usage tracker released the spool file, and doesn't open a new
      // one.
      val afterClose = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(0, afterClose.lockedFiles.size.toLong())
      assertEquals(1, afterClose.completedLogs.size.toLong())

      // Check that there is exactly one spool file with one event logged that equals the event
      // we logged.
      for ((_, value) in afterClose.completedLogs) {
        assertEquals(1, value.size.toLong())
        val logEvent = value[0]
        val actualEvent = studioEventFromLogEvent(logEvent)
        assertEquals(logEntry.build(), actualEvent)
      }
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }


  @Test
  @Throws(Exception::class)
  fun trackerVersionTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // Setup up an instance of the JournalingUsageTracker using a temp spool directory and
      //  virtual time scheduler.
      val virtualTimeScheduler = VirtualTimeScheduler()
      val journalingUsageTracker = JournalingUsageTracker(
        virtualTimeScheduler,
        testSpoolDir.root.toPath())

      // Set version on the usage tracker.
      UsageTracker.version = "1.2.3.4"

      // Create a log entry and log it.
      val logEntry = createAndroidStudioEvent(42)
      journalingUsageTracker.logNow(logEntry)
      // Ensure this triggers an action on the scheduler and run the action
      assertEquals(1, virtualTimeScheduler.actionsQueued)
      virtualTimeScheduler.advanceBy(0)
      assertEquals(0, virtualTimeScheduler.actionsQueued)

      // The action should have written to the still locked spool file.
      val beforeClose = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, beforeClose.lockedFiles.size.toLong())
      assertEquals(0, beforeClose.completedLogs.size.toLong())

      // Close the usage tracker
      journalingUsageTracker.close()

      // Ensure that closing the usage tracker released the spool file, and doesn't open a new
      // one.
      val afterClose = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(0, afterClose.lockedFiles.size.toLong())
      assertEquals(1, afterClose.completedLogs.size.toLong())

      // Check that there is exactly one spool file with one event logged and that that
      // event has the version specified on the usage tracker set .
      for ((_, value) in afterClose.completedLogs) {
        assertEquals(1, value.size.toLong())
        val logEvent = value[0]
        val actualEvent = studioEventFromLogEvent(logEvent)
        assertTrue(actualEvent.hasProductDetails())
        assertEquals("1.2.3.4", actualEvent.productDetails.version)
      }
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun trackerTimeoutTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // Setup up an instance of the JournalingUsageTracker using a temp spool directory and
      // virtual time scheduler.
      val virtualTimeScheduler = VirtualTimeScheduler()
      val journalingUsageTracker = JournalingUsageTracker(
        virtualTimeScheduler,
        testSpoolDir.root.toPath())
      UsageTracker.setWriterForTest(journalingUsageTracker)

      // Set a timeout of 1 minute for closing the current spool file.
      UsageTracker.setMaxJournalTime(1, TimeUnit.MINUTES)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // Write an event to the usage tracker
      val logEntry1 = createAndroidStudioEvent(22)
      journalingUsageTracker.logNow(logEntry1)
      // Run the scheduler to write the log to the journal file
      virtualTimeScheduler.advanceBy(0)

      // Before the timeout there should be one spool file and it should be locked.
      val beforeTimeout = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, beforeTimeout.lockedFiles.size.toLong())
      assertEquals(0, beforeTimeout.completedLogs.size.toLong())

      // Advance the scheduler for the timeout to occur.
      val actionsExecuted = virtualTimeScheduler.advanceBy(1, TimeUnit.MINUTES)
      assertEquals(1, actionsExecuted)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // After the timeout there should be one spool file that is locked and one that is ready for reading.
      // the latter should contain the event logged before the timeout.
      val afterTimeout = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, afterTimeout.lockedFiles.size.toLong())
      assertEquals(1, afterTimeout.completedLogs.size.toLong())

      for ((_, value) in afterTimeout.completedLogs) {
        assertEquals(1, value.size.toLong())
        val logEvent = value[0]
        val actualEvent = studioEventFromLogEvent(logEvent)
        assertEquals(logEntry1.build(), actualEvent)
      }

      // Log another event.
      val logEntry2 = createAndroidStudioEvent(33)
      journalingUsageTracker.logNow(logEntry2)
      virtualTimeScheduler.advanceBy(0)

      // Close the scheduler for flushing any outstanding spool files.
      journalingUsageTracker.close()

      // Check that the expected jobs have been executed.
      assertEquals(3, virtualTimeScheduler.actionsExecuted)
      assertEquals(0, virtualTimeScheduler.actionsQueued)

      // After close we expect two seperate spool files, each containing one of the events.
      val afterClose = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(0, afterClose.lockedFiles.size.toLong())
      assertEquals(2, afterClose.completedLogs.size.toLong())

      for ((key, value) in afterTimeout.completedLogs) {
        val existingAfterClose = afterClose.completedLogs[key]
        assertEquals(existingAfterClose, value)
        afterClose.completedLogs.remove(key)
      }

      for ((_, value) in afterClose.completedLogs) {
        assertEquals(1, value.size.toLong())
        val logEvent = value[0]
        val actualEvent = studioEventFromLogEvent(logEvent)
        assertEquals(logEntry2.build(), actualEvent)
      }

      // Closing again should be a noop.
      journalingUsageTracker.close()
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
      UsageTracker.cleanAfterTesting()
    }
  }

  @Test
  @Throws(Exception::class)
  fun trackerTimeoutNoLogsTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // Setup up an instance of the JournalingUsageTracker using a temp spool directory and
      // virtual time scheduler.
      val virtualTimeScheduler = VirtualTimeScheduler()
      val journalingUsageTracker = JournalingUsageTracker(
        virtualTimeScheduler,
        testSpoolDir.root.toPath())
      UsageTracker.setWriterForTest(journalingUsageTracker)

      // Set a timeout of 1 minute for closing the current spool file.
      UsageTracker.setMaxJournalTime(1, TimeUnit.MINUTES)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // Before the timeout there should be one spool file and it should be locked.
      val beforeTimeout = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, beforeTimeout.lockedFiles.size.toLong())
      assertEquals(0, beforeTimeout.completedLogs.size.toLong())

      // Advance the scheduler for the timeout to occur.
      val actionsExecuted = virtualTimeScheduler.advanceBy(1, TimeUnit.MINUTES)
      assertEquals(1, actionsExecuted)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // After the timeout there should not be any changes to the spool files as
      // there was nothing to log.
      val afterTimeout = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, afterTimeout.lockedFiles.size.toLong())
      assertEquals(0, afterTimeout.completedLogs.size.toLong())

      journalingUsageTracker.close()
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
      UsageTracker.cleanAfterTesting()
    }
  }

  @Test
  @Throws(Exception::class)
  fun trackerMaxLogsTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // Setup up an instance of the JournalingUsageTracker using a temp spool directory and
      // virtual time scheduler.
      val virtualTimeScheduler = VirtualTimeScheduler()
      val journalingUsageTracker = JournalingUsageTracker(
        virtualTimeScheduler,
        testSpoolDir.root.toPath())

      // Restrict the max amount of logs per spool file to 3.
      UsageTracker.maxJournalSize = 3
      assertEquals(0, virtualTimeScheduler.actionsQueued)

      // Write two events.
      val event1 = createAndroidStudioEvent(1)
      journalingUsageTracker.logNow(event1)
      virtualTimeScheduler.advanceBy(0)

      val event2 = createAndroidStudioEvent(2)
      journalingUsageTracker.logNow(event2)
      virtualTimeScheduler.advanceBy(0)

      // Ensure that given we haven't reach max, there is only one spool file and it is locked.
      val beforeMax = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, beforeMax.lockedFiles.size.toLong())
      assertEquals(0, beforeMax.completedLogs.size.toLong())

      // Write another event
      val event3 = createAndroidStudioEvent(3)
      journalingUsageTracker.logNow(event3)
      virtualTimeScheduler.advanceBy(0)

      // Ensure we hit max that the original spool file has completed and a new one created and
      // locked.
      val afterMax = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, afterMax.lockedFiles.size.toLong())
      assertEquals(1, afterMax.completedLogs.size.toLong())

      for ((_, value) in afterMax.completedLogs) {
        assertEquals(3, value.size.toLong())
        val actualEvent1 = studioEventFromLogEvent(value[0])
        assertEquals(event1.build(), actualEvent1)
        val actualEvent2 = studioEventFromLogEvent(value[1])
        assertEquals(event2.build(), actualEvent2)
        val actualEvent3 = studioEventFromLogEvent(value[2])
        assertEquals(event3.build(), actualEvent3)
      }

      // Write two more events.
      val event4 = createAndroidStudioEvent(4)
      journalingUsageTracker.logNow(event4)
      virtualTimeScheduler.advanceBy(0)

      val event5 = createAndroidStudioEvent(5)
      journalingUsageTracker.logNow(event5)
      virtualTimeScheduler.advanceBy(0)

      // Close the usage tracker.
      journalingUsageTracker.close()

      // After close we expect two spool files, the first with the first 3 events and the second
      // file the last 2 events.
      val afterClose = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(0, afterClose.lockedFiles.size.toLong())
      assertEquals(2, afterClose.completedLogs.size.toLong())

      for ((key, value) in afterMax.completedLogs) {
        val existingAfterClose = afterClose.completedLogs[key]
        assertEquals(existingAfterClose, value)
        afterClose.completedLogs.remove(key)
      }

      for ((_, value) in afterClose.completedLogs) {
        assertEquals(2, value.size.toLong())
        val actualEvent4 = studioEventFromLogEvent(value[0])
        assertEquals(event4.build(), actualEvent4)
        val actualEvent5 = studioEventFromLogEvent(value[1])
        assertEquals(event5.build(), actualEvent5)
      }
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun trackerUpdateTimeoutTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // Setup up an instance of the JournalingUsageTracker using a temp spool directory and
      // virtual time scheduler.
      val virtualTimeScheduler = VirtualTimeScheduler()
      val journalingUsageTracker = JournalingUsageTracker(
        virtualTimeScheduler,
        testSpoolDir.root.toPath())
      UsageTracker.setWriterForTest(journalingUsageTracker)

      // Write an event to ensure track file switch is triggered.
      val event = createAndroidStudioEvent(1)
      journalingUsageTracker.logNow(event)
      virtualTimeScheduler.advanceBy(0)

      // Set a timeout of 1 minute for closing the current spool file.
      UsageTracker.setMaxJournalTime(1, TimeUnit.MINUTES)
      assertEquals(1, virtualTimeScheduler.actionsExecuted)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // Move time forward but not enough to trigger the timeout.
      virtualTimeScheduler.advanceBy(30, TimeUnit.SECONDS)
      assertEquals(1, virtualTimeScheduler.actionsExecuted)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // Update the timeout
      UsageTracker.setMaxJournalTime(1, TimeUnit.MINUTES)
      assertEquals(1, virtualTimeScheduler.actionsExecuted)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // Move to the time of the original timeout
      virtualTimeScheduler.advanceBy(30, TimeUnit.SECONDS)
      // Ensure the original timeout is not triggered.
      assertEquals(1, virtualTimeScheduler.actionsExecuted)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // Move to the time of the new timeout
      virtualTimeScheduler.advanceBy(30, TimeUnit.SECONDS)
      // Ensure the new timeout is triggered.
      assertEquals(2, virtualTimeScheduler.actionsExecuted)
      assertEquals(1, virtualTimeScheduler.actionsQueued)

      // Ensure that the first spool file was closed and a new one created.
      val afterTimeout = getSpoolDetails(testSpoolDir.root.toPath())
      assertEquals(1, afterTimeout.lockedFiles.size.toLong())
      assertEquals(1, afterTimeout.completedLogs.size.toLong())
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
      UsageTracker.cleanAfterTesting()
    }
  }

  @Test
  @Throws(IOException::class)
  fun updateSettingsAndTrackerTest() {
    val beforeUpdate = UsageTracker.writerForTest
    assertTrue(beforeUpdate is NullUsageTracker)
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())
    try {
      val settingsFile = testConfigDir.root.toPath().resolve("analytics.settings").toFile()
      assertFalse(settingsFile.exists())

      // Setup up an instance of the JournalingUsageTracker using a temp spool directory and
      // virtual time scheduler.
      val virtualTimeScheduler = VirtualTimeScheduler()

      // updating to opt-in false from scratch should set NullUsageTracker
      // and initialize settings.
      UsageTracker.updateSettingsAndTracker(
        false, StdLogger(StdLogger.Level.INFO), virtualTimeScheduler)
      assertTrue(settingsFile.exists())
      val afterFirstUpdate = UsageTracker.writerForTest
      assertTrue(afterFirstUpdate is NullUsageTracker)
      assertFalse(AnalyticsSettings.optedIn)

      // updating to opt-in true should update settings and initialize JournalingUsageTracker.
      val settings2 = UsageTracker.updateSettingsAndTracker(
        true, StdLogger(StdLogger.Level.INFO), virtualTimeScheduler)
      val afterSecondUpdate = UsageTracker.writerForTest
      assertTrue(afterSecondUpdate is JournalingUsageTracker)
      assertTrue(AnalyticsSettings.optedIn)
      assertEquals(virtualTimeScheduler,
                   (UsageTracker.writerForTest as JournalingUsageTracker).scheduler)

      // updating to opt-in false should update settings and initialize NullUsageTracker.
      UsageTracker.updateSettingsAndTracker(
        false, StdLogger(StdLogger.Level.INFO), virtualTimeScheduler)
      val afterThirdUpdate = UsageTracker.writerForTest
      assertTrue(afterThirdUpdate is NullUsageTracker)
      assertFalse(AnalyticsSettings.optedIn)

      // now that we have a NullTracker, no spool files should be locked.
      assertTrue(getSpoolDetails(testSpoolDir.root.toPath()).lockedFiles.isEmpty())
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun eventTimeTest() {
    // create virtual time for scheduling and expected time events are logged
    val virtualTimeScheduler = VirtualTimeScheduler()
    // move time ahead by one minutes to use as the start time of the application/logging framework
    virtualTimeScheduler.advanceBy(1, TimeUnit.MINUTES)
    val dateProvider = VirtualTimeDateProvider(virtualTimeScheduler)
    UsageTracker.dateProvider = dateProvider
    try {
      val journalingUsageTracker = JournalingUsageTracker(
        virtualTimeScheduler,
        testSpoolDir.root.toPath())

      UsageTracker.setWriterForTest(journalingUsageTracker)
      // move time ahead by two minutes after creating the usage tracker to use as current time of event logged.
      virtualTimeScheduler.advanceBy(2, TimeUnit.MINUTES)

      // Create a log entry and log it.
      val event = createAndroidStudioEvent(42)
      journalingUsageTracker.logNow(event)
      // close the tracker so we can read the results from disk
      virtualTimeScheduler.advanceBy(0)
      journalingUsageTracker.close()

      // read spooled log entries
      val results = getSpoolDetails(testSpoolDir.root.toPath())
      // there should only be one track file.
      assertEquals(1, results.completedLogs.size.toLong())
      for ((_, value) in results.completedLogs) {
        // with only one log entry
        assertEquals(1, value.size.toLong())
        val logEntry = value[0]
        // the application should be running for two minutes
        assertEquals(TimeUnit.MINUTES.toMillis(2), logEntry.eventUptimeMs)
        // and the event should be recorded at 3 minutes since epoch.
        assertEquals(TimeUnit.MINUTES.toMillis(3), logEntry.eventTimeMs)
      }
    }
    finally {
      UsageTracker.dateProvider = DateProvider.SYSTEM
      UsageTracker.cleanAfterTesting()
    }
  }

  /**
   * Helper that builds a [AndroidStudioEvent] with a marker to distinguish this message.
   */
  private fun createAndroidStudioEvent(marker: Long): AndroidStudioEvent.Builder {
    return AndroidStudioEvent.newBuilder()
      .setCategory(AndroidStudioEvent.EventCategory.META)
      .setKind(AndroidStudioEvent.EventKind.META_METRICS)
      .setMetaMetrics(
        MetaMetrics.newBuilder()
          .setBytesSentInLastUpload(marker)
          .setFailedConnections(0)
          .setFailedServerReplies(0))
  }

  /**
   * Helper that examins the provided spool directory and reports on locked vs completed spool
   * files. For completed spool files, it parses the contents and provides the protobuf messages
   * in that spool file.
   */
  @Throws(IOException::class)
  private fun getSpoolDetails(testSpoolDir: Path): SpoolDetails {
    val spoolDetails = SpoolDetails()
    val stream = Files.newDirectoryStream(testSpoolDir, "*.trk")
    for (trackFile in stream) {
      val channel = RandomAccessFile(trackFile.toFile(), "rw").channel
      try {
        val lock = channel.tryLock()
        if (lock != null) {
          val inputStream = Channels.newInputStream(channel)
          val entries = ArrayList<ClientAnalytics.LogEvent>()
          while (true) {
            val event = ClientAnalytics.LogEvent.parseDelimitedFrom(inputStream) ?: break
            entries.add(event)
          }
          spoolDetails.completedLogs[trackFile] = entries
          lock.close()
          channel.close()
        }
        else {
          spoolDetails.lockedFiles.add(trackFile)
        }
      }
      catch (e: OverlappingFileLockException) {
        spoolDetails.lockedFiles.add(trackFile)
      }

    }
    return spoolDetails
  }

  /**
   * Helper that parses the binary blob of a [ClientAnalytics.LogEvent] into an [ ].
   */
  @Throws(InvalidProtocolBufferException::class)
  private fun studioEventFromLogEvent(logEvent: ClientAnalytics.LogEvent): AndroidStudioEvent {
    return AndroidStudioEvent.parseFrom(logEvent.sourceExtension)
  }
}
