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
    AnalyticsStateManager.loggedInUser = null
  }

  @Test
  fun `initial state is NONE`() {
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.NONE)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `data sharing true, no user, level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `data sharing false, user set, level is NONE`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.LOGGED_IN)
    assertThat(state.loggedInUser).isNotNull()
  }

  @Test
  fun `data sharing true, user set, level is LOGGED_IN`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.LOGGED_IN)
    assertThat(state.loggedInUser).isNotNull()
  }

  @Test
  fun `clear LoggedInUser from LOGGED_IN, level is ANONYMOUS`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.loggedInUser = null
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.ANONYMOUS)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `clear LoggedInUser from NONE , level is NONE`() {
    AnalyticsStateManager.dataSharing = false
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.loggedInUser = null
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.NONE)
    assertThat(state.loggedInUser).isNull()
  }

  @Test
  fun `clear data sharing from LOGGED_IN, level is NONE`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.loggedInUser = LoggedInUser("test@google.com") { "token" }
    AnalyticsStateManager.dataSharing = false
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.NONE)
    assertThat(state.loggedInUser).isNotNull()
  }

  @Test
  fun `clear data sharing from ANONYMOUS, level is NONE`() {
    AnalyticsStateManager.dataSharing = true
    AnalyticsStateManager.loggedInUser = null
    AnalyticsStateManager.dataSharing = false
    val state = AnalyticsStateManager.analyticsStateFlow.value
    assertThat(state.level).isEqualTo(AnalyticsLevel.NONE)
    assertThat(state.loggedInUser).isNull()
  }
}
