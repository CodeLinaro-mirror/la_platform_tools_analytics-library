/*
 * Copyright (C) 2018 The Android Open Source Project
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

import com.android.annotations.VisibleForTesting
import com.android.utils.DateProvider
import com.android.utils.ILogger
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.io.*
import java.math.BigInteger
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Paths
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ScheduledExecutorService

/**
 * Settings related to analytics reporting. These settings are stored in
 * ~/.android/analytics.settings as a json file.
 */
object AnalyticsSettings {
  private var initialized = false

  @JvmStatic
  val userId : String
    get() {
      synchronized(gate) {
        ensureInitialized()
        return instance?.userId ?: ""
      }
    }

  @JvmStatic
  var optedIn : Boolean
    get() {
      synchronized(gate) {
        ensureInitialized()
        return instance?.optedIn ?: false
      }
    }
  set(value) {
    synchronized(gate) {
      instance?.apply {
        optedIn = value
      }
    }
  }

  private fun ensureInitialized() {
    if (!initialized && java.lang.Boolean.getBoolean("idea.is.internal")) {
      // Android Studio Developers: If you hit this exception, you're trying to find out the status
      // of AnalyticsSettings before the system has been initialized. Please reach out the the owners
      // of this code to figure out how best to do these checks instead of getting null values.
      throw RuntimeException("call to AnalyticsSettings before initialization")
    }
  }

  @JvmStatic
  val debugDisablePublishing: Boolean
    get() {
      synchronized(gate) {
        ensureInitialized()
        return instance?.debugDisablePublishing ?: false
      }
    }

  internal const val SALT_SKEW_NOT_INITIALIZED = -1

  @VisibleForTesting
  @JvmStatic
  var dateProvider: DateProvider = DateProvider.SYSTEM

  private val EPOCH = LocalDate.ofEpochDay(0)
  // the gate is used to ensure settings are accessed single-threaded
  private val gate = Any()

  @JvmStatic
  private var instance: AnalyticsSettingsData? = null

  /**
   * Gets the current salt skew, this is used by [.getSalt] to update the salt every 28
   * days with a consistent window. This window size allows 4 week and 1 week analyses.
   */
  @VisibleForTesting
  @JvmStatic
  fun currentSaltSkew(): Int {
    val now = LocalDate.from(
      Instant.ofEpochMilli(dateProvider.now().time).atZone(ZoneOffset.UTC))
    // Unix epoch was on a Thursday, but we want Monday to be the day the salt is refreshed.
    val days = ChronoUnit.DAYS.between(EPOCH, now) + 3
    return (days / 28).toInt()
  }

  /**
   * Loads an existing settings file from disk, or creates a new valid settings object if none
   * exists. In case of the latter, will try to load uid.txt for maintaining the same uid with
   * previous metrics reporting.
   *
   * @throws IOException if there are any issues reading the settings file.
   */
  @VisibleForTesting
  @Throws(IOException::class)
  @JvmStatic
  private fun loadSettingsData(logger: ILogger): AnalyticsSettingsData {
    val file = settingsFile
    if (!file.exists()) {
      return createNewAnalyticsSettingsData()
    }
    val channel = RandomAccessFile(file, "rw").channel
    try {
      lateinit var settings: AnalyticsSettingsData
      channel.tryLock().use {
        val inputStream = Channels.newInputStream(channel)
        val gson = GsonBuilder().create()
        settings = gson.fromJson(InputStreamReader(inputStream), AnalyticsSettingsData::class.java)
      }
      if (!isValid(settings)) {
        return createNewAnalyticsSettingsData()
      }
      return settings
    }
    catch (e: OverlappingFileLockException) {
      logger.error(e, "Unable to lock settings file %s", file.toString())
    }
    catch (e: JsonParseException) {
      logger.error(e,"Unable to parse settings file %s", file.toString())
    }
    var newSettings = AnalyticsSettingsData()
    newSettings.userId = UUID.randomUUID().toString()
    return newSettings
  }

  /**
   * Creates a new settings object and writes it to disk. Will try to load uid.txt for maintaining
   * the same uid with previous metrics reporting.
   *
   * @throws IOException if there are any issues writing the settings file.
   */
  @VisibleForTesting
  @JvmStatic
  @Throws(IOException::class)
  private fun createNewAnalyticsSettingsData(): AnalyticsSettingsData {
    val settings = AnalyticsSettingsData()

    val uidFile = Paths.get(AnalyticsPaths.getAndEnsureAndroidSettingsHome(), "uid.txt").toFile()
    if (uidFile.exists()) {
      try {
        val uid = Files.readFirstLine(uidFile, Charsets.UTF_8)
        settings.userId = uid
      }
      catch (e: IOException) {
        // Ignore and set new UID.
      }

    }
    if (settings.userId == null) {
      settings.userId = UUID.randomUUID().toString()
    }
    settings.saveSettings()
    return settings
  }

  @JvmStatic
  var googlePlayDateProvider: WebServerDateProvider? = null

  /**
   * Get or creates an instance of the settings. Uses the following strategies in order:
   *
   *
   *  * Use existing instance
   *  * Load existing 'analytics.settings' file from disk
   *  * Create new 'analytics.settings' file
   *  * Create instance without persistence
   *
   *
   * Any issues reading/writing the config file will be logged to the logger.
   */
  @JvmStatic
  @JvmOverloads
  fun initialize(logger: ILogger, scheduler: ScheduledExecutorService? = null) {
    synchronized(gate) {
      if (instance != null) {
        return
      }
      initialized = true
      instance = loadSettingsData(logger)
    }
    scheduler?.submit {
      try {
        val gp = WebServerDateProvider(URL("https://play.google.com/"))
        dateProvider = gp
        googlePlayDateProvider = gp
      } catch(e: IOException) {
        logger.error(e, "Unable to get current time from Google's servers")
      }
    }
  }

  /**
   * Allows test to set a custom version of the AnalyticsSettings to test different setting
   * states.
   */
  @VisibleForTesting
  @JvmStatic
  fun setInstanceForTest(settings: AnalyticsSettingsData?) {
    synchronized(gate) {
      instance = settings
      initialized = instance != null
    }
  }

  /**
   * Helper to get the file to read/write settings from based on the configured android settings
   * home.
   */
  internal val settingsFile: File
    get() = Paths.get(AnalyticsPaths.getAndEnsureAndroidSettingsHome(), "analytics.settings").toFile()


  /**
   * Gets a binary blob to ensure per user anonymization. Gets automatically rotated every 28
   * days. Primarily used by [Anonymizer].
   */
  val salt: ByteArray
    @Throws(IOException::class)
    get() = synchronized(AnalyticsSettings.gate) {
      var data: AnalyticsSettingsData = instance ?: return byteArrayOf()
      val currentSaltSkew = AnalyticsSettings.currentSaltSkew()
      if (data.saltSkew != currentSaltSkew) {
        data.saltSkew = currentSaltSkew
        val random = SecureRandom()
        val blob = ByteArray(24)
        random.nextBytes(blob)
        data.saltValue = BigInteger(blob)
        saveSettings()
      }
      val blob = data.saltValue.toByteArray()
      var fullBlob = blob
      if (blob.size < 24) {
        fullBlob = ByteArray(24)
        System.arraycopy(blob, 0, fullBlob, 0, blob.size)
      }
      return fullBlob
    }

  /**
   * Writes this settings object to disk.
   * @throws IOException if there are any issues writing the settings file.
   */
  @JvmStatic
  @Throws(IOException::class)
  fun saveSettings() {
    ensureInitialized()
    instance?.saveSettings()
  }

  /** Checks if the AnalyticsSettings object is in a valid state.  */
  internal fun isValid(settings : AnalyticsSettingsData): Boolean {
    return settings.userId != null && (settings.saltSkew == AnalyticsSettings.SALT_SKEW_NOT_INITIALIZED || settings.saltValue != null)
  }
}

class AnalyticsSettingsData {
  fun saveSettings() {
    val file = AnalyticsSettings.settingsFile
    var dir = file.parentFile
    if (!dir.exists()) {
      dir.mkdirs()
    }
    try {
      RandomAccessFile(file, "rw").use { settingsFile ->
        settingsFile.channel.use { channel ->
          channel.tryLock().use { lock ->
            if (lock == null) {
              throw IOException("Unable to lock settings file " + file.toString())
            }
            channel.truncate(0)
            val outputStream = Channels.newOutputStream(channel)
            val gson = GsonBuilder().create()
            val writer = OutputStreamWriter(outputStream)
            gson.toJson(this, writer)
            writer.flush()
            outputStream.flush()
          }
        }
      }
    }
    catch (e: OverlappingFileLockException) {
      throw IOException("Unable to lock settings file " + file.toString(), e)
    }
  }

  /**
   * User id used for reporting analytics. This id is pseudo-anonymous.
   */
  @field:SerializedName("userId")
  public var userId: String? = null

  @field:SerializedName("hasOptedIn")
  public var optedIn: Boolean = false

  @field:SerializedName("debugDisablePublishing")
  public val debugDisablePublishing: Boolean = false

  @field:SerializedName("saltValue")
  public var saltValue = BigInteger.valueOf(0L)

  @field:SerializedName("saltSkew")
  public var saltSkew = AnalyticsSettings.SALT_SKEW_NOT_INITIALIZED


}



