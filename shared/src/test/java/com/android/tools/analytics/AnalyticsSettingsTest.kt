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

import com.android.tools.analytics.stubs.StubDateProvider
import com.android.utils.DateProvider
import com.android.utils.ILogger
import com.google.common.base.Charsets
import com.google.protobuf.ByteString
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Arrays
import java.util.Date
import java.util.UUID
import kotlin.io.path.readText
import kotlin.io.path.writeText
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

/** Tests for [AnalyticsSettings]. */
class AnalyticsSettingsTest {

  object failureLogger : ILogger {

    override fun error(t: Throwable?, msgFormat: String?, vararg args: Any) {
      fail("${msgFormat?.format(*args)} throwable=$t")
    }

    override fun warning(msgFormat: String, vararg args: Any) {
      fail(msgFormat.format(*args))
    }

    override fun info(msgFormat: String, vararg args: Any) {
      fail(msgFormat.format(*args))
    }

    override fun verbose(msgFormat: String, vararg args: Any) {
      fail(msgFormat.format(*args))
    }
  }

  class CountingLogger : ILogger {

    var errors = 0
    var warnings = 0
    var infos = 0
    var verboses = 0

    override fun error(t: Throwable?, msgFormat: String?, vararg args: Any) {
      errors++
    }

    override fun warning(msgFormat: String, vararg args: Any) {
      warnings++
    }

    override fun info(msgFormat: String, vararg args: Any) {
      infos++
    }

    override fun verbose(msgFormat: String, vararg args: Any) {
      verboses++
    }
  }

  @get:Rule var testConfigDir = TemporaryFolder()

  @get:Rule var thrown = ExpectedException.none()

  private val analyticsSettingsFile: Path
    get() = testConfigDir.root.toPath().resolve("analytics.settings")

  private var analyticsSettingsFileContent: String
    get() = analyticsSettingsFile.readText()
    set(value) = analyticsSettingsFile.writeText(value)

  @Before
  fun setup() {
    // Configure the paths to use a temp directory for reading from and writing to.
    AnalyticsPaths.overrideAndroidSettingsHomeDirectory(testConfigDir.root.toString())
  }

  @After
  fun cleanup() {
    AnalyticsPaths.restoreAndroidSettingsHomeDirectory()
  }

  @Test
  @Throws(Exception::class)
  fun loadExistingSettingsTest() {
    // Write a json settings file.
    analyticsSettingsFileContent = "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", hasOptedIn: true }"

    // read settings just written.
    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)

    // verify read settings.
    assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", AnalyticsSettings.userId)
    assertTrue(AnalyticsSettings.optedIn)

    // Write another json settings file
    analyticsSettingsFileContent = "{ userId: \"06120264-c9e7-492f-a39c-89c3cbee57c5\", hasOptedIn: false }"
    // read settings just written.
    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)

    // verify read settings are updated.
    assertEquals("06120264-c9e7-492f-a39c-89c3cbee57c5", AnalyticsSettings.userId)
    assertFalse(AnalyticsSettings.optedIn)
  }

  @Test
  @Throws(Exception::class)
  fun loadBadSettingsTest() {
    // Write non-valid json file content.
    analyticsSettingsFileContent = "BADFILE"

    AnalyticsSettings.setInstanceForTest(null)
    val countingLogger = CountingLogger()
    AnalyticsSettings.initialize(countingLogger)

    // Verify that a warning has been logged
    assertEquals(1, countingLogger.warnings)

    AnalyticsSettings.setInstanceForTest(null)
    countingLogger.warnings = 0
    AnalyticsSettings.initialize(countingLogger)

    // Verify that no warnings were logged
    assertEquals(0, countingLogger.warnings)
  }

  @Test
  @Throws(Exception::class)
  fun loadCorruptedSettingsTest() {
    // Write non-valid json file content.
    analyticsSettingsFileContent =
      "{\"hasOptedIn\":true,\"saltValue\":746227786052768374406922174584132630757738414714263142088,\"saltSkew\":632}"

    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)
    // Try reading the settings file and verify that it fails.
    assertFalse(AnalyticsSettings.optedIn)
  }

  @Test
  @Throws(Exception::class)
  fun loadCorruptedEmptySettingsTest() {
    // Write empty file.
    analyticsSettingsFileContent = ""

    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)
    // Try reading the settings file and verify that it fails.
    assertFalse(AnalyticsSettings.optedIn)
  }

  @Test
  @Throws(Exception::class)
  fun loadBadDateFormatSettingsTest() {
    // Write empty file.
    analyticsSettingsFileContent =
      "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", hasOptedIn: true, \"lastSentimentQuestionDate\":\"Jan 12, 2023, 8:52:49 AM\" }"

    AnalyticsSettings.setInstanceForTest(null)
    val countingLogger = CountingLogger()
    AnalyticsSettings.initialize(countingLogger)
    // Try reading the settings file and verify that it fails.
    assertFalse(AnalyticsSettings.optedIn)

    // Verify that a warning has been logged
    assertEquals(1, countingLogger.warnings)
  }

  @Test
  @Throws(Exception::class)
  fun newSettingsTest() {
    assertFalse(Files.exists(analyticsSettingsFile))
    // load settings while there is no settings file present.
    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)

    // The generated user id should be a valid UUID.
    UUID.fromString(AnalyticsSettings.userId)

    val uid = AnalyticsSettings.userId
    // Default setting should be to not be opted in.
    assertFalse(AnalyticsSettings.optedIn)

    // The settings file should now be created.
    assertTrue(analyticsSettingsFileContent.isNotEmpty())

    // Reading the settings again should lead to the same data being read.
    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)
    assertFalse(AnalyticsSettings.optedIn)
    assertEquals(uid, AnalyticsSettings.userId)
    assertEquals(
      """{"userId":"<uuid>","hasOptedIn":false,"debugDisablePublishing":false,"saltValue":0,"saltSkew":-1}""",
      analyticsSettingsFileContent.normalizeUserid().normalizeSalt(),
    )
    assertTrue(BigInteger(AnalyticsSettings.salt) != BigInteger.ZERO)
    assertEquals(
      """{"userId":"<uuid>","hasOptedIn":false,"debugDisablePublishing":false,"saltValue":<saltValue>,"saltSkew":<saltSkew>}""",
      analyticsSettingsFileContent.normalizeUserid().normalizeSalt(),
    )
  }

  @Test
  @Throws(Exception::class)
  fun loadNewSettingsWithExistingUserIdTest() {
    // create a 'uid.txt' file, used by previous metrics reporting systems.
    val uid = "db3dd15b-053a-4066-ac93-04c50585edc2"
    Files.write(testConfigDir.root.toPath().resolve("uid.txt"), uid.toByteArray(Charsets.UTF_8))

    // create new settings.
    AnalyticsSettings.setInstanceForTest(null)
    val settings = AnalyticsSettings.initialize(failureLogger)
    assertNotNull(settings)

    // Ensure the settings are using the user id from the 'uid.txt' file.
    assertEquals(uid, AnalyticsSettings.userId)

    // Default setting should be to not be opted in.
    assertFalse(AnalyticsSettings.optedIn)
    assertEquals(
      """{"userId":"db3dd15b-053a-4066-ac93-04c50585edc2","hasOptedIn":false,"debugDisablePublishing":false,"saltValue":0,"saltSkew":-1}""",
      analyticsSettingsFileContent.normalizeSalt(),
    )
    assertTrue(BigInteger(AnalyticsSettings.salt) != BigInteger.ZERO)
    // Getting the salt should trigger the file to be updated with the new salt value
    assertEquals(
      """{"userId":"db3dd15b-053a-4066-ac93-04c50585edc2","hasOptedIn":false,"debugDisablePublishing":false,"saltValue":<saltValue>,"saltSkew":<saltSkew>}""",
      analyticsSettingsFileContent.normalizeSalt(),
    )
  }

  @Test
  @Throws(Exception::class)
  fun changeSettingsTest() {
    // Start with an existing config on disk.
    analyticsSettingsFileContent = "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", hasOptedIn: true }"

    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)

    assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", AnalyticsSettings.userId)
    assertTrue(AnalyticsSettings.optedIn)

    AnalyticsSettings.optedIn = false

    // Write updated settings to disk
    AnalyticsSettings.saveSettings()

    // Read settings and verify that changes have persisted.
    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)
    assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", AnalyticsSettings.userId)
    assertFalse(AnalyticsSettings.optedIn)
  }

  @Test
  fun saltSkewTest() {
    try {
      // Stub dates to specific dates around boundaries when we expect the salt skew to change.
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 3, 17)
      assertEquals(603, AnalyticsSettings.currentSaltSkew().toLong())
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 3, 18)
      assertEquals(604, AnalyticsSettings.currentSaltSkew().toLong())
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 4, 15)
      assertEquals(604, AnalyticsSettings.currentSaltSkew().toLong())
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 4, 16)
      assertEquals(605, AnalyticsSettings.currentSaltSkew().toLong())
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 5, 12)
      assertEquals(605, AnalyticsSettings.currentSaltSkew().toLong())
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 5, 13)
      assertEquals(606, AnalyticsSettings.currentSaltSkew().toLong())
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 6, 10)
      assertEquals(606, AnalyticsSettings.currentSaltSkew().toLong())
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 6, 11)
      assertEquals(607, AnalyticsSettings.currentSaltSkew().toLong())
    } finally {
      // undo stubbing of dates.
      AnalyticsSettings.dateProvider = DateProvider.SYSTEM
    }
  }

  @Test
  @Throws(IOException::class)
  fun saltStickinessTest() {
    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)

    // Stub dates to be at specific skew
    AnalyticsSettings.dateProvider = StubDateProvider(2018, 10, 1)
    // get the salt and ensure it is initialized.
    val initialSalt = AnalyticsSettings.salt
    assertNotNull(initialSalt)
    assertEquals(24, initialSalt.size.toLong())
    // Ensure the salt is still the same at the end of the skew date range.
    AnalyticsSettings.dateProvider = StubDateProvider(2018, 10, 25)
    assertArrayEquals(initialSalt, AnalyticsSettings.salt)

    // Ensure the salt is different in the next skew date range.
    AnalyticsSettings.dateProvider = StubDateProvider(2018, 10, 26)
    val newSalt = AnalyticsSettings.salt
    assertNotNull(newSalt)
    assertEquals(24, newSalt.size.toLong())
    assertFalse(Arrays.equals(initialSalt, newSalt))

    // Ensure the salt is  same at the 532 end of this new range
    AnalyticsSettings.dateProvider = StubDateProvider(2020, 4, 10)
    val endOfNewSalt = AnalyticsSettings.salt
    assertNotNull(endOfNewSalt)
    assertEquals(24, endOfNewSalt.size.toLong())
    assertArrayEquals(newSalt, endOfNewSalt)

    // Ensure the salt rotates after 532 days
    AnalyticsSettings.dateProvider = StubDateProvider(2020, 4, 11)
    val startOfEvenNewerSalt = AnalyticsSettings.salt
    assertNotNull(startOfEvenNewerSalt)
    assertEquals(24, startOfEvenNewerSalt.size.toLong())
    assertFalse(Arrays.equals(endOfNewSalt, startOfEvenNewerSalt))

    AnalyticsSettings.saveSettings()

    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)
    val loadedSalt = AnalyticsSettings.salt
    assertArrayEquals(startOfEvenNewerSalt, loadedSalt)

    // Ensure the rotated salt respects the content on the disk
    AnalyticsSettings.dateProvider = StubDateProvider(2022, 4, 11)
    File(AnalyticsPaths.getAndEnsureAndroidSettingsHome(), "analytics.settings")
      .writeText(
        """
        {"userId":"user-id","hasOptedIn":false,"debugDisablePublishing":false,"saltValue":12345,"saltSkew":683}
        """
          .trimIndent()
      )
    val saltFromDisk = AnalyticsSettings.salt
    assertNotNull(saltFromDisk)
    assertEquals(24, saltFromDisk.size.toLong())
    assertEquals(ByteString.copyFrom(BigInteger("12345").toByteArrayOfLength24()), ByteString.copyFrom(saltFromDisk))
  }

  @Test
  @Throws(IOException::class)
  fun getInstanceTest() {
    AnalyticsSettings.setInstanceForTest(null)
    // create a 'uid.txt' file, used by previous metrics reporting systems.
    val uid = "db3dd15b-053a-4066-ac93-04c50585edc2"
    Files.write(testConfigDir.root.toPath().resolve("uid.txt"), uid.toByteArray(Charsets.UTF_8))

    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)
    assertEquals(uid, AnalyticsSettings.userId)
    assertFalse(AnalyticsSettings.optedIn)

    AnalyticsSettings.optedIn = true
    // Write updated settings to disk
    AnalyticsSettings.saveSettings()
    assertEquals(
      """{"userId":"<uuid>","hasOptedIn":true,"debugDisablePublishing":false,"saltValue":0,"saltSkew":-1}""",
      analyticsSettingsFileContent.normalizeUserid().normalizeSalt(),
    )

    // Read settings and verify that changes have persisted.
    AnalyticsSettings.setInstanceForTest(null)
    AnalyticsSettings.initialize(failureLogger)
    assertEquals(uid, AnalyticsSettings.userId)
    assertTrue(AnalyticsSettings.optedIn)
  }

  @Test
  fun uninitializedTest() {
    AnalyticsSettings.setInstanceForTest(null)
    // without the idea.is.internal property AnalyticsSettings should return false when
    // uninitialized
    assertFalse(AnalyticsSettings.optedIn)

    // with the idea.is.internal property. AnalyticsSettings should throw when uninitialized
    System.setProperty("idea.is.internal", "true")
    try {
      AnalyticsSettings.optedIn
      fail("should have thrown RuntimeException")
    } catch (_: RuntimeException) {
      // expected
    }
    // with the idea.is.internal property but initialized, data should be returned as normal
    val data = AnalyticsSettingsData()
    data.optedIn = true
    AnalyticsSettings.setInstanceForTest(data)
    assertTrue(AnalyticsSettings.optedIn)
  }

  @Test
  fun analyticsDisabledTest() {
    // Write a json settings file.
    val json = "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", hasOptedIn: true }"
    analyticsSettingsFileContent = json
    // ensure the settings are not initialized
    AnalyticsSettings.setInstanceForTest(null)
    // disable analytics
    AnalyticsSettings.disable()

    assertTrue(AnalyticsSettings.initialized)
    assertFalse(AnalyticsSettings.optedIn)
    assertEquals("", AnalyticsSettings.userId)
    assertEquals(json, analyticsSettingsFileContent) // The analytics settings file is not touched
  }

  @Test
  fun useJava8DateFormat() {
    AnalyticsSettings.setInstanceForTest(
      AnalyticsSettingsData().apply {
        userId = "db3dd15b-053a-4066-ac93-04c50585edc2"
        optedIn = true
        lastSentimentAnswerDate = Date(115, 4, 17, 14, 23, 45)
      }
    )
    AnalyticsSettings.saveSettings()

    assertEquals(
      """{"userId":"<uuid>","hasOptedIn":true,"debugDisablePublishing":false,"saltValue":0,"saltSkew":-1,"lastSentimentAnswerDate":"May 17, 2015 2:23:45 PM"}""",
      analyticsSettingsFileContent.normalizeUserid().normalizeSalt(),
    )
  }

  @Test
  fun settingsDataIncludesAllFields() {
    val allFieldsSettingsContent =
      """{"userId":"db3dd15b-053a-4066-ac93-04c50585edc2","hasOptedIn":true,"debugDisablePublishing":true,"saltValue":1234,"saltSkew":567,"lastSentimentQuestionDate":"May 17, 2015 2:23:45 PM","lastSentimentAnswerDate":"May 18, 2015 2:23:45 PM","lastFeatureSurveyDate":"May 19, 2015 2:23:45 PM","lastFeatureSurveyDateMap":{"survey1":"May 20, 2015 2:23:45 PM"},"lastOptinPromptVersion":"2020.3.4"}"""

    analyticsSettingsFileContent = allFieldsSettingsContent

    val settingsData =
      FileChannel.open(analyticsSettingsFile, StandardOpenOption.READ, StandardOpenOption.WRITE).use { channel ->
        AnalyticsSettingsData.parseSettingsData(channel, analyticsSettingsFile.toFile(), failureLogger)!!
      }
    assertEquals("db3dd15b-053a-4066-ac93-04c50585edc2", settingsData.userId)
    assertEquals(true, settingsData.optedIn)
    assertEquals(true, settingsData.debugDisablePublishing)
    assertEquals(BigInteger.valueOf(1234), settingsData.saltValue)
    assertEquals(567, settingsData.saltSkew)
    assertEquals(Date(115, 4, 17, 14, 23, 45), settingsData.lastSentimentQuestionDate)
    assertEquals(Date(115, 4, 18, 14, 23, 45), settingsData.lastSentimentAnswerDate)
    assertEquals(Date(115, 4, 19, 14, 23, 45), settingsData.nextFeatureSurveyDate)
    assertEquals(mapOf("survey1" to Date(115, 4, 20, 14, 23, 45)), settingsData.nextFeatureSurveyDateMap)
    assertEquals("2020.3.4", settingsData.lastOptinPromptVersion)

    settingsData.saveSettings(failureLogger)

    assertEquals(allFieldsSettingsContent, analyticsSettingsFileContent)
  }

  @Test
  fun testResetUserId() {
    AnalyticsSettings.setInstanceForTest(AnalyticsSettingsData().apply { userId = "db3dd15b-053a-4066-ac93-04c50585edc2" })
    AnalyticsSettings.saveSettings()
    AnalyticsSettings.resetUserId()
    // We can't check the actual value because the new value is a randomly generated UUID
    // Confirm that the value has not stayed the same
    assertNotEquals(AnalyticsSettings.userId, "db3dd15b-053a-4066-ac93-04c50585edc2")

    assertEquals(
      """{"userId":"<uuid>","hasOptedIn":false,"debugDisablePublishing":false,"saltValue":0,"saltSkew":-1}""",
      analyticsSettingsFileContent.normalizeUserid(),
    )
  }

  private fun String.normalizeUserid(): String {
    return this.replace(regex = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"), replacement = "<uuid>")
  }

  private fun String.normalizeSalt(): String {
    return this.replace(regex = Regex(""""saltValue":-?\d{2,}"""), replacement = "\"saltValue\":<saltValue>")
      .replace(regex = Regex(""""saltSkew":\d{2,}"""), replacement = "\"saltSkew\":<saltSkew>")
  }
}
