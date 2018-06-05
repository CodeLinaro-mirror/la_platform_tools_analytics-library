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
import com.google.gson.JsonParseException
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.nio.file.Files
import java.util.*

/**
 * Tests for [AnalyticsSettings].
 */
class AnalyticsSettingsTest {
  @get:Rule
  var testConfigDir = TemporaryFolder()

  @get:Rule
  var thrown = ExpectedException.none()

  @Test
  @Throws(Exception::class)
  fun loadExistingSettingsTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(testConfigDir.root.toString())
    try {
      // Write a json settings file.
      val json = "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", optedIn: true }"
      Files.write(
        testConfigDir.root.toPath().resolve("analytics.settings"),
        json.toByteArray(Charsets.UTF_8))

      // read settings just written.
      val settings = AnalyticsSettings.loadSettings()
      assertNotNull(settings)

      // verify read settings.
      assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", settings!!.userId)
      assertTrue(settings.optedIn)

      // Write another json settings file
      val json2 = "{ userId: \"06120264-c9e7-492f-a39c-89c3cbee57c5\", optedIn: false }"
      Files.write(
        testConfigDir.root.toPath().resolve("analytics.settings"),
        json2.toByteArray(Charsets.UTF_8))
      val settings2 = AnalyticsSettings.loadSettings()
      assertNotNull(settings2)

      // verify read settings are updated.
      assertEquals("06120264-c9e7-492f-a39c-89c3cbee57c5", settings2!!.userId)
      assertFalse(settings2.optedIn)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun loadBadSettingsTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())
    try {
      // Write non-valid json file content.
      val json = "BADFILE"
      Files.write(
        testConfigDir.root.toPath().resolve("analytics.settings"),
        json.toByteArray(Charsets.UTF_8))

      // try reading the settings file and verify that it fails.
      thrown.expect(IOException::class.java)
      thrown.expectCause(IsInstanceOf.instanceOf(JsonParseException::class.java))
      AnalyticsSettings.loadSettings()
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun loadCorruptedSettingsTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())
    try {
      // Write non-valid json file content.
      val json = "{\"optedIn\":true,\"saltValue\":746227786052768374406922174584132630757738414714263142088,\"saltSkew\":632}"
      Files.write(
        testConfigDir.root.toPath().resolve("analytics.settings"),
        json.toByteArray(Charsets.UTF_8))

      val settings = AnalyticsSettings.loadSettings()
      // Try reading the settings file and verify that it fails.
      assertNotNull(settings)
      assertFalse(settings!!.optedIn)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun newSettingsTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())
    try {
      // load settings while there is no settings file present.
      val settings = AnalyticsSettings.loadSettings()
      // The generated user id should be a valid UUID.

      UUID.fromString(settings!!.userId!!)

      // Default setting should be to not be opted in.
      assertFalse(settings.optedIn)

      // The settings file should now be created.
      assertTrue(
        testConfigDir
          .root
          .toPath()
          .resolve("analytics.settings")
          .toFile()
          .exists())

      settings.saveSettings()

      // The settings file should still exist.
      assertTrue(
        testConfigDir
          .root
          .toPath()
          .resolve("analytics.settings")
          .toFile()
          .exists())

      // Reading the settings again should lead to the same data being read.
      val settings2 = AnalyticsSettings.loadSettings()
      assertNotNull(settings2)

      assertEquals(settings.userId, settings2!!.userId)
      assertFalse(settings2.optedIn)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun loadNewSettingsWithExistingUserIdTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // create a 'uid.txt' file, used by previous metrics reporting systems.
      val uid = "db3dd15b-053a-4066-ac93-04c50585edc2"
      Files.write(
        testConfigDir.root.toPath().resolve("uid.txt"),
        uid.toByteArray(Charsets.UTF_8))

      // create new settings.
      val settings = AnalyticsSettings.createNewAnalyticsSettings()
      assertNotNull(settings)

      // Ensure the settings are using the user id from the 'uid.txt' file.
      assertEquals(uid, settings.userId)

      // Default setting should be to not be opted in.
      assertFalse(settings.optedIn)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(Exception::class)
  fun changeSettingsTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    try {
      // Start with an existing config on disk.
      val json = "{ userId: \"a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16\", optedIn: true }"
      Files.write(
        testConfigDir.root.toPath().resolve("analytics.settings"),
        json.toByteArray(Charsets.UTF_8))

      val settings = AnalyticsSettings.loadSettings()
      assertNotNull(settings)

      assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", settings!!.userId)
      assertTrue(settings.optedIn)

      // Update properties in the settings.
      val newUserId = "79d30adf-c901-4608-83ca-6dc850068316"
      settings.userId = newUserId
      settings.optedIn = false

      // Write updated settings to disk
      settings.saveSettings()

      // Read settings and verify that changes have persisted.
      val settings2 = AnalyticsSettings.loadSettings()
      assertNotNull(settings2)
      assertEquals(newUserId, settings2!!.userId)
      assertFalse(settings2.optedIn)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  fun saltSkewTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())
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
    }
    finally {
      // undo stubbing of dates.
      AnalyticsSettings.dateProvider = DateProvider.SYSTEM
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(IOException::class)
  fun saltStickinessTest() {
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(testConfigDir.root.toString())
    try {
      var settings: AnalyticsSettings? = AnalyticsSettings()
      settings!!.userId = UUID.randomUUID().toString()

      // Stub dates to be at specific skew
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 3, 18)
      // get the salt and ensure it is initialized.
      val initialSalt = settings.salt
      assertNotNull(initialSalt)
      assertEquals(24, initialSalt.size.toLong())
      // Ensure the salt is still the same at the end of the skew date range.
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 4, 15)
      assertArrayEquals(initialSalt, settings.salt)

      // Ensure the salt is different in the next skew date range.
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 4, 16)
      val newSalt = settings.salt
      assertNotNull(newSalt)
      assertEquals(24, newSalt.size.toLong())
      assertFalse(Arrays.equals(initialSalt, newSalt))
      settings.saveSettings()

      settings = AnalyticsSettings.loadSettings()
      val loadedSalt = settings!!.salt
      assertArrayEquals(newSalt, loadedSalt)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  @Test
  @Throws(IOException::class)
  fun getInstanceTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(
      testConfigDir.root.toPath().toString())

    val logger = object : ILogger {
      override fun error(
        t: Throwable?, msgFormat: String?, vararg args: Any) {
        fail()
      }

      override fun warning(msgFormat: String, vararg args: Any) {
        fail()
      }

      override fun info(msgFormat: String, vararg args: Any) {
        fail()
      }

      override fun verbose(msgFormat: String, vararg args: Any) {
        fail()
      }
    }

    try {
      AnalyticsSettings.setInstanceForTest(null)
      // create a 'uid.txt' file, used by previous metrics reporting systems.
      val uid = "db3dd15b-053a-4066-ac93-04c50585edc2"
      Files.write(
        testConfigDir.root.toPath().resolve("uid.txt"),
        uid.toByteArray(Charsets.UTF_8))

      val settings = AnalyticsSettings.getInstance(logger)
      assertEquals(uid, settings.userId)
      assertFalse(settings.optedIn)

      settings.optedIn = true
      // Write updated settings to disk
      settings.saveSettings()

      // Read settings and verify that changes have persisted.
      val settings2 = AnalyticsSettings.loadSettings()
      assertNotNull(settings2)
      assertEquals(uid, settings2!!.userId)
      assertTrue(settings2.optedIn)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }
}
