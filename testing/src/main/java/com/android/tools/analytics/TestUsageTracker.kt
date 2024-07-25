package com.android.tools.analytics

import com.android.testutils.VirtualTimeDateProvider
import com.android.testutils.VirtualTimeScheduler
import com.android.utils.FileUtils
import com.google.common.io.Files
import com.google.protobuf.InvalidProtocolBufferException
import com.google.wireless.android.play.playlog.proto.ClientAnalytics
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Level
import java.util.logging.Logger

/**
 * An implementation of [UsageTracker] for use in tests. Allows introspection of the logged usages
 * via [TestUsageTracker.usages] and [TestUsageTracker.listener].
 */
class TestUsageTracker(val scheduler: VirtualTimeScheduler) : UsageTrackerWriter() {
  /**
   * All the recorded usages. The elements might *not* be sorted chronologically. You should check
   * [LoggedUsage.timestamp] and sort manually if needed.
   */
  val usages: CopyOnWriteArrayList<LoggedUsage> = CopyOnWriteArrayList()

  /**
   * A listener to notify new usage. You can optionally set the listener from your test and
   * [TestUsageTrackerListener.onNewUsage] will be invoked once a new usage is arrived.
   */
  var listener: TestUsageTrackerListener? = null

  private val androidSdkHomeEnvironment: File
  private var closeException: RuntimeException? = null

  private val logger: Logger
    get() = Logger.getLogger("#TestUsageTracker")

  init {
    // in order to ensure reproducible anonymized values & timestamps are reported,
    // set a date provider based on the virtual time scheduler.
    val dateProvider = VirtualTimeDateProvider(scheduler)
    AnalyticsSettings.dateProvider = dateProvider
    androidSdkHomeEnvironment = Files.createTempDir()
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(androidSdkHomeEnvironment.path)
  }

  override fun logDetails(logEvent: ClientAnalytics.LogEvent.Builder) {
    try {
      val newUsage = LoggedUsage(scheduler.currentTimeNanos, logEvent.build())
      usages.add(newUsage)
      listener?.onNewUsage(newUsage)
    } catch (e: InvalidProtocolBufferException) {
      throw RuntimeException("Expecting a LogEvent that contains an AndroidStudioEvent proto", e)
    }
  }

  @Throws(Exception::class)
  override fun close() {
    if (closeException != null) {
      logger.log(Level.SEVERE, "Re-closing TestUsageTracker. Last closed by:", closeException)
      throw closeException!!
    }
    closeException = RuntimeException("Last TestUsageTracker close")

    // Clean up the virtual time data provider after the test is done.
    val dateProvider = VirtualTimeDateProvider(scheduler)
    AnalyticsSettings.dateProvider = dateProvider
    FileUtils.deleteDirectoryContents(androidSdkHomeEnvironment)
    Environment.instance = Environment.SYSTEM
  }

  override fun flush() {}
}

/** An interface to listen a new log usages. */
interface TestUsageTrackerListener {
  /**
   * When a new usage is arrived, this method is invoked. It is guaranteed that
   * [TestUsageTracker.usages] is updated before this callback is invoked.
   */
  fun onNewUsage(loggedUsage: LoggedUsage)
}
