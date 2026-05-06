/*
 * Copyright (C) 2026 The Android Open Source Project
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

import com.android.utils.StdLogger
import com.google.common.truth.Truth
import java.util.concurrent.Executors
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AnalyticsPublisherInitializationTest {
  private val analyticsSettingsData = AnalyticsSettingsData()
  private val scheduledExecutorService = Executors.newScheduledThreadPool(1)
  private val logger = StdLogger(StdLogger.Level.ERROR)
  private val applicationBuild = "1.2.3.4"
  @get:Rule val temporaryFolder = TemporaryFolder()

  @Before
  fun setUp() {
    analyticsSettingsData.optedIn = false
    AnalyticsSettings.setInstanceForTest(analyticsSettingsData)
    AnalyticsPaths.overrideAndroidSettingsHomeDirectory(temporaryFolder.newFolder().absolutePath)
    AnalyticsStateManager.dataSharing = false
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = null
  }

  @After
  fun cleanUp() {
    AnalyticsPaths.restoreAndroidSettingsHomeDirectory()
    AnalyticsStateManager.dataSharing = false
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = null
    AnalyticsPublisher.reset()
  }

  @Test
  fun testInitializeFunctionOptedIn() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsPublisher.initialize(logger, scheduledExecutorService, applicationBuild)

    assertHasAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()
  }

  @Test
  fun testInitializeFunctionOptedOut() {
    AnalyticsStateManager.dataSharing = false
    AnalyticsPublisher.initialize(logger, scheduledExecutorService, applicationBuild)

    assertDoesNotHaveAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()
  }

  @Test
  fun testMultipleInitializations() {
    AnalyticsStateManager.dataSharing = false
    AnalyticsPublisher.initialize(logger, scheduledExecutorService, applicationBuild)

    assertDoesNotHaveAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()

    AnalyticsStateManager.dataSharing = true
    AnalyticsPublisher.initialize(logger, scheduledExecutorService, applicationBuild)

    assertHasAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()

    AnalyticsStateManager.dataSharing = false
    AnalyticsPublisher.initialize(logger, scheduledExecutorService, applicationBuild)

    assertDoesNotHaveAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()
  }

  @Test
  fun testLoggedInPublisher() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsPublisher.initialize(logger, scheduledExecutorService, applicationBuild)

    assertHasAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()

    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsPublisher.updateState()

    assertHasAnonymousPublisher()
    assertHasLoggedInPublisher()

    AnalyticsStateManager.loggedInUser = null
    AnalyticsPublisher.updateState()

    assertHasAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()
  }

  // TODO(b/438541344): Remove this test once Sherlock has been updated.
  @Test
  fun testOptedInSetting() {
    AnalyticsSettings.optedIn = true
    AnalyticsStateManager.dataSharing = false
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = null
    AnalyticsPublisher.initialize(logger, scheduledExecutorService, applicationBuild)

    AnalyticsPublisher.updateState()
    assertHasAnonymousPublisher()
    assertDoesNotHaveLoggedInPublisher()
  }

  fun assertHasAnonymousPublisher() {
    Truth.assertThat(AnalyticsPublisher.anonymousInstance).isInstanceOf(AnonymousAnalyticsPublisher::class.java)
  }

  fun assertDoesNotHaveAnonymousPublisher() {
    Truth.assertThat(AnalyticsPublisher.anonymousInstance).isNotInstanceOf(AnonymousAnalyticsPublisher::class.java)
  }

  fun assertHasLoggedInPublisher() {
    Truth.assertThat(AnalyticsPublisher.loggedInInstance).isInstanceOf(LoggedInAnalyticsPublisher::class.java)
  }

  fun assertDoesNotHaveLoggedInPublisher() {
    Truth.assertThat(AnalyticsPublisher.loggedInInstance).isNotInstanceOf(LoggedInAnalyticsPublisher::class.java)
  }
}
