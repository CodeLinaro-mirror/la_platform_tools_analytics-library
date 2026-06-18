/*
 * Copyright (C) 2020 The Android Open Source Project
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

import com.google.common.truth.Truth
import java.util.concurrent.Executors
import org.junit.After
import org.junit.Before
import org.junit.Test

class UsageTrackerInitializationTest {
  private val scheduledExecutorService = Executors.newScheduledThreadPool(1)

  @Before
  fun setUp() {
    AnalyticsStateManager.dataSharing = false
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = null
    UsageTracker.updateState()
  }

  @After
  fun cleanUp() {
    UsageTracker.cleanAfterTesting()
  }

  @Test
  fun testInitializeFunctionOptedIn() {
    AnalyticsStateManager.dataSharing = true
    UsageTracker.initialize(scheduledExecutorService)

    UsageTracker.assertHasAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()
  }

  @Test
  fun testInitializeFunctionOptedOut() {
    AnalyticsStateManager.dataSharing = false
    UsageTracker.initialize(scheduledExecutorService)

    UsageTracker.assertDoesNotHaveAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()
  }

  @Test
  fun testMultipleInitializations() {
    AnalyticsStateManager.dataSharing = false
    UsageTracker.initialize(scheduledExecutorService)

    UsageTracker.assertDoesNotHaveAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()

    AnalyticsStateManager.dataSharing = true
    UsageTracker.initialize(scheduledExecutorService)

    UsageTracker.assertHasAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()

    AnalyticsStateManager.dataSharing = true
    UsageTracker.initialize(scheduledExecutorService)

    UsageTracker.assertHasAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()

    AnalyticsStateManager.dataSharing = false
    UsageTracker.initialize(scheduledExecutorService)

    UsageTracker.assertDoesNotHaveAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()
  }

  @Test
  fun testLoggedInWriter() {
    AnalyticsStateManager.dataSharing = true
    UsageTracker.initialize(scheduledExecutorService)

    UsageTracker.assertHasAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()

    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.emailConsent = true
    UsageTracker.updateState()

    UsageTracker.assertHasAnonymousWriter()
    UsageTracker.assertHasLoggedInWriter()

    AnalyticsStateManager.loggedInUser = null
    UsageTracker.updateState()

    UsageTracker.assertHasAnonymousWriter()
    UsageTracker.assertDoesNotHaveLoggedInWriter()
  }

  fun UsageTracker.assertHasAnonymousWriter() {
    Truth.assertThat(this.anonymousWriter).isInstanceOf(AnonymousUsageTrackerWriter::class.java)
  }

  fun UsageTracker.assertDoesNotHaveAnonymousWriter() {
    Truth.assertThat(this.anonymousWriter).isNotInstanceOf(AnonymousUsageTrackerWriter::class.java)
  }

  fun UsageTracker.assertHasLoggedInWriter() {
    Truth.assertThat(this.loggedInWriter).isInstanceOf(LoggedInUsageTrackerWriter::class.java)
  }

  fun UsageTracker.assertDoesNotHaveLoggedInWriter() {
    Truth.assertThat(this.loggedInWriter).isNotInstanceOf(LoggedInUsageTrackerWriter::class.java)
  }
}
