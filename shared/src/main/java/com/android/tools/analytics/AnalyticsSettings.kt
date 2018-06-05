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
import java.nio.channels.Channels
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Paths
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Settings related to analytics reporting. These settings are stored in
 * ~/.android/analytics.settings as a json file.
 */
class AnalyticsSettings {

  /**
   * User id used for reporting analytics. This id is pseudo-anonymous.
   */
  @field:SerializedName("userId")
  var userId: String? = null

  @field:SerializedName("optedIn")
  var optedIn: Boolean = false

  @field:SerializedName("debugDisablePublishing")
  val debugDisablePublishing: Boolean = false

  @field:SerializedName("saltValue")
  private var saltValue = BigInteger.valueOf(0L)

  @field:SerializedName("saltSkew")
  private var saltSkew = SALT_SKEW_NOT_INITIALIZED

  /**
   * Gets a binary blob to ensure per user anonymization. Gets automatically rotated every 28
   * days. Primarily used by [Anonymizer].
   */
  val salt: ByteArray
    @Throws(IOException::class)
    get() = synchronized(gate) {
      val currentSaltSkew = currentSaltSkew()
      if (saltSkew != currentSaltSkew) {
        saltSkew = currentSaltSkew
        val random = SecureRandom()
        val data = ByteArray(24)
        random.nextBytes(data)
        saltValue = BigInteger(data)
        saveSettings()
      }
      val blob = saltValue.toByteArray()
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
  @Throws(IOException::class)
  fun saveSettings() {
    val file = settingsFile
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

  /** Checks if the AnalyticsSettings object is in a valid state.  */
  private fun isValid(): Boolean {
    return userId != null && (saltSkew == SALT_SKEW_NOT_INITIALIZED || saltValue != null)
  }

  companion object {
    private const val SALT_SKEW_NOT_INITIALIZED = -1

    @VisibleForTesting
    @JvmStatic
    var dateProvider: DateProvider = DateProvider.SYSTEM

    private val EPOCH = LocalDate.ofEpochDay(0)
    // the gate is used to ensure settings are only in process of loading once.
    @Transient
    private val gate = Any()

    @JvmStatic
    private var instance: AnalyticsSettings? = null

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
    fun loadSettings(): AnalyticsSettings? {
      val file = settingsFile
      if (!file.exists()) {
        return createNewAnalyticsSettings()
      }
      val channel = RandomAccessFile(file, "rw").channel
      try {
        lateinit var settings: AnalyticsSettings
        channel.tryLock().use {
          val inputStream = Channels.newInputStream(channel)
          val gson = GsonBuilder().create()
          settings = gson.fromJson(InputStreamReader(inputStream), AnalyticsSettings::class.java)
          instance = settings
        }
        if (!settings.isValid()) {
          return createNewAnalyticsSettings()
        }
        return settings
      }
      catch (e: OverlappingFileLockException) {
        throw IOException("Unable to lock settings file " + file.toString(), e)
      }
      catch (e: JsonParseException) {
        throw IOException("Unable to parse settings file " + file.toString(), e)
      }

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
    fun createNewAnalyticsSettings(): AnalyticsSettings {
      val settings = AnalyticsSettings()

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
    fun getInstance(logger: ILogger): AnalyticsSettings {
      synchronized(gate) {
        if (instance != null) {
          return instance!!
        }
        var loaded: AnalyticsSettings? = null
        try {
          loaded = loadSettings()
        }
        catch (e: IOException) {
          logger.info("Unable to load analytics settings: %s", e.message)
        }

        if (loaded == null) {
          try {
            loaded = createNewAnalyticsSettings()
          }
          catch (e: IOException) {
            logger.info("Unable to create new analytics settings: %s", e.message)
          }
        }
        if (loaded == null) {
          loaded = AnalyticsSettings()
          loaded.userId = UUID.randomUUID().toString()
        }
        return loaded
      }
    }

    /**
     * Allows test to set a custom version of the AnalyticsSettings to test different setting
     * states.
     */
    @VisibleForTesting
    @JvmStatic
    fun setInstanceForTest(settings: AnalyticsSettings?) {
      instance = settings
    }

    /**
     * Helper to get the file to read/write settings from based on the configured android settings
     * home.
     */
    private val settingsFile: File
      get() = Paths.get(AnalyticsPaths.getAndEnsureAndroidSettingsHome(), "analytics.settings").toFile()
  }
}
