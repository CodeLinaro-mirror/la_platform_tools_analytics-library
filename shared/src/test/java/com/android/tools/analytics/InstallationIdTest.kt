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
import java.util.UUID
import java.util.prefs.Preferences
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

private const val NODE_NAME = "google"
private const val INSTALLATION_ID_KEY = "user_id_on_machine"

@RunWith(JUnit4::class)
class InstallationIdTest {

  private val mockUserRootPrefs = mock(Preferences::class.java)
  private val mockNodePrefs = mock(Preferences::class.java)

  @Test
  fun get_existingValidId_returnsExistingId() {
    val existingUuid = UUID.randomUUID().toString()
    `when`(mockUserRootPrefs.node(NODE_NAME)).thenReturn(mockNodePrefs)
    `when`(mockNodePrefs.get(eq(INSTALLATION_ID_KEY), anyString())).thenReturn(existingUuid)

    val result = InstallationId.get(mockUserRootPrefs)

    assertThat(result).isEqualTo(existingUuid)
    verify(mockUserRootPrefs).node(NODE_NAME)
    verify(mockNodePrefs).get(INSTALLATION_ID_KEY, "")
    verify(mockNodePrefs, never()).put(anyString(), anyString())
  }

  @Test
  fun get_noExistingId_generatesAndSavesNewId() {
    `when`(mockUserRootPrefs.node(NODE_NAME)).thenReturn(mockNodePrefs)
    `when`(mockNodePrefs.get(eq(INSTALLATION_ID_KEY), anyString())).thenReturn("")

    val result = InstallationId.get(mockUserRootPrefs)

    assertThat(result).isNotEmpty()
    assertThat(result.isValidUuid()).isTrue() // Check if the result is a valid UUID
    verify(mockUserRootPrefs).node(NODE_NAME)
    verify(mockNodePrefs).get(INSTALLATION_ID_KEY, "")
    verify(mockNodePrefs).put(eq(INSTALLATION_ID_KEY), argThat { it.isValidUuid() })
  }

  @Test
  fun get_invalidExistingId_generatesAndSavesNewId() {
    val invalidUuid = "not-a-valid-uuid"
    `when`(mockUserRootPrefs.node(NODE_NAME)).thenReturn(mockNodePrefs)
    `when`(mockNodePrefs.get(eq(INSTALLATION_ID_KEY), anyString())).thenReturn(invalidUuid)

    val result = InstallationId.get(mockUserRootPrefs)

    assertThat(result).isNotEmpty()
    assertThat(result).isNotEqualTo(invalidUuid)
    assertThat(result.isValidUuid()).isTrue() // Check if the result is a valid UUID
    verify(mockUserRootPrefs).node(NODE_NAME)
    verify(mockNodePrefs).get(INSTALLATION_ID_KEY, "")
    verify(mockNodePrefs).put(eq(INSTALLATION_ID_KEY), argThat { it.isValidUuid() })
  }

  private fun String.isValidUuid(): Boolean {
    return try {
      UUID.fromString(this)
      true
    } catch (_: IllegalArgumentException) {
      false
    }
  }
}
