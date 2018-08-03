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
  object failureLogger : ILogger {
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

  object countingLogger : ILogger {
    var errors = 0
    var warnings = 0
    var infos = 0
    var verboses = 0
    override fun error(
      t: Throwable?, msgFormat: String?, vararg args: Any) {
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
      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)

      // verify read settings.
      assertEquals("a4d47d92-8d4c-44bb-a8a4-d2483b6e0c16", AnalyticsSettings.userId)
      assertTrue(AnalyticsSettings.optedIn)

      // Write another json settings file
      val json2 = "{ userId: \"06120264-c9e7-492f-a39c-89c3cbee57c5\", optedIn: false }"
      Files.write(
        testConfigDir.root.toPath().resolve("analytics.settings"),
        json2.toByteArray(Charsets.UTF_8))
      // read settings just written.
      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)

      // verify read settings are updated.
      assertEquals("06120264-c9e7-492f-a39c-89c3cbee57c5", AnalyticsSettings.userId)
      assertFalse(AnalyticsSettings.optedIn)
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

      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(countingLogger)

      assertEquals(1, countingLogger.errors)
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

      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)
      // Try reading the settings file and verify that it fails.
      assertFalse(AnalyticsSettings.optedIn)
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
    // The settings file should now be created.
    assertFalse(
      testConfigDir
        .root
        .toPath()
        .resolve("analytics.settings")
        .toFile()
        .exists())

    try {
      // load settings while there is no settings file present.
      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)

      // The generated user id should be a valid UUID.
      UUID.fromString(AnalyticsSettings.userId)

      val uid = AnalyticsSettings.userId
      // Default setting should be to not be opted in.
      assertFalse(AnalyticsSettings.optedIn)

      // The settings file should now be created.
      assertTrue(
        testConfigDir
          .root
          .toPath()
          .resolve("analytics.settings")
          .toFile()
          .exists())

      //AnalyticsSettings.saveSettings()

      // The settings file should still exist.
      assertTrue(
        testConfigDir
          .root
          .toPath()
          .resolve("analytics.settings")
          .toFile()
          .exists())

      // Reading the settings again should lead to the same data being read.
      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)
      assertFalse(AnalyticsSettings.optedIn)
      assertEquals(uid, AnalyticsSettings.userId)
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
      AnalyticsSettings.setInstanceForTest(null)
      val settings = AnalyticsSettings.initialize(failureLogger)
      assertNotNull(settings)

      // Ensure the settings are using the user id from the 'uid.txt' file.
      assertEquals(uid, AnalyticsSettings.userId)

      // Default setting should be to not be opted in.
      assertFalse(AnalyticsSettings.optedIn)
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
      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)

      // Stub dates to be at specific skew
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 3, 18)
      // get the salt and ensure it is initialized.
      val initialSalt = AnalyticsSettings.salt
      assertNotNull(initialSalt)
      assertEquals(24, initialSalt.size.toLong())
      // Ensure the salt is still the same at the end of the skew date range.
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 4, 15)
      assertArrayEquals(initialSalt, AnalyticsSettings.salt)

      // Ensure the salt is different in the next skew date range.
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 4, 16)
      val newSalt = AnalyticsSettings.salt
      assertNotNull(newSalt)
      assertEquals(24, newSalt.size.toLong())
      assertFalse(Arrays.equals(initialSalt, newSalt))
      AnalyticsSettings.saveSettings()

      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)
      val loadedSalt = AnalyticsSettings.salt
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

    try {
      AnalyticsSettings.setInstanceForTest(null)
      // create a 'uid.txt' file, used by previous metrics reporting systems.
      val uid = "db3dd15b-053a-4066-ac93-04c50585edc2"
      Files.write(
        testConfigDir.root.toPath().resolve("uid.txt"),
        uid.toByteArray(Charsets.UTF_8))

      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)
      assertEquals(uid, AnalyticsSettings.userId)
      assertFalse(AnalyticsSettings.optedIn)

      AnalyticsSettings.optedIn = true
      // Write updated settings to disk
      AnalyticsSettings.saveSettings()

      // Read settings and verify that changes have persisted.
      AnalyticsSettings.setInstanceForTest(null)
      AnalyticsSettings.initialize(failureLogger)
      assertEquals(uid, AnalyticsSettings.userId)
      assertTrue(AnalyticsSettings.optedIn)
    }
    finally {
      EnvironmentFakes.setSystemEnvironment()
    }
  }
}
