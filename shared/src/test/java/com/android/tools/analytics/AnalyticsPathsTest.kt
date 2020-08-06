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

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Paths

/**
 * Tests for [AnalyticsPaths].
 */
class AnalyticsPathsTest {
  @get:Rule
  var testConfigDir = TemporaryFolder()

  @After
  @Throws(Exception::class)
  fun setSystemEnvironment() {
    EnvironmentFakes.setSystemEnvironment()
  }

  @Test
  @Throws(Exception::class)
  fun getAndroidSettingsHomeTest() {
    // Test picking the default path ~/.android/ when no environment variable exists.
    EnvironmentFakes.setNoEnvironmentVariable()
    assertEquals(
      Paths.get(Environment.instance.getSystemProperty(Environment.SystemProperty.USER_HOME)!!, ".android").toString(),
      AnalyticsPaths.getAndEnsureAndroidSettingsHome())

    // Test using the ANDROID_SDK_HOME environment variable.
    val customRoot = "/a/b/c"
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(customRoot)
    assertEquals(customRoot, AnalyticsPaths.getAndEnsureAndroidSettingsHome())

    // Test using the ANDROID_SDK_HOME as a system property.
    val property = Environment.SystemProperty.ANDROID_SDK_HOME
    val prev = Environment.instance.getSystemProperty(property)
    System.setProperty(property.key, customRoot)
    assertEquals(customRoot, AnalyticsPaths.getAndEnsureAndroidSettingsHome())
    if (prev != null) {
      System.setProperty(property.key, prev)
    }
    else {
      System.clearProperty(property.key)
    }
  }

  @Test
  @Throws(Exception::class)
  fun getAndroidSettingsHomeCreated() {
    val customPath = testConfigDir.root.toPath().resolve(".android").toString()
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(customPath)
    assertEquals(customPath, AnalyticsPaths.getAndEnsureAndroidSettingsHome())
    val androidHomeFile = File(customPath)
    assertTrue(androidHomeFile.exists())
    assertTrue(androidHomeFile.isDirectory)
  }

  @Test
  @Throws(Exception::class)
  fun getSpoolDirectoryTest() {
    // Test picking the default path under ~/.android/ when no environment variable exists.
    EnvironmentFakes.setNoEnvironmentVariable()
    assertEquals(
      Paths.get(Environment.instance.getSystemProperty(Environment.SystemProperty.USER_HOME)!!, ".android", "metrics", "spool").toString(),
      AnalyticsPaths.spoolDirectory)

    // Test using the ANDROID_SDK_HOME environment variable.
    val customRoot = "/a/b/c"
    EnvironmentFakes.setCustomAndroidSdkHomeEnvironment(customRoot)
    assertEquals(Paths.get(customRoot, "metrics", "spool").toString(), AnalyticsPaths.spoolDirectory)
  }
}
