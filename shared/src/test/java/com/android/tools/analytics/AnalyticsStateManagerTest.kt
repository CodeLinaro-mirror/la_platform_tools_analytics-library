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

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class AnalyticsStateManagerTest {
  @Before
  fun setup() {
    AnalyticsStateManager.dataSharing = false
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = null
  }

  @Test
  fun `initial state is NONE`() {
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.NONE)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `dataSharing true, no user, no consent, level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `dataSharing true, no user, with consent, level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = true
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `dataSharing true, user set, no consent, level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNotNull()
  }

  @Test
  fun `dataSharing true, user set, with consent, level is LOGGED_IN`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.LOGGED_IN)
    assertThat(state.loggedInUser).isNotNull()
  }

  @Test
  fun `clear LoggedInUser from LOGGED_IN, level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.loggedInUser = null
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `clear LoggedInUser from ANONYMOUS (with user, no consent), level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.loggedInUser = null
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `dataSharing false from LOGGED_IN, level is NONE`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.dataSharing = false
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.NONE)
    assertThat(state.loggedInUser).isNotNull()
  }

  @Test
  fun `revoke emailConsent from LOGGED_IN, level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.emailConsent = false
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNotNull()
  }

  @Test
  fun `grant emailConsent with user set, level is LOGGED_IN`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.emailConsent = false
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.emailConsent = true
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.LOGGED_IN)
    assertThat(state.loggedInUser).isNotNull()
  }
}
