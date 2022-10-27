/*
 * Copyright (C) 2019 The Android Open Source Project
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

import org.junit.Test
import com.google.common.truth.Truth.assertThat
import com.google.wireless.android.sdk.stats.TestLibraries.newBuilder
import com.google.wireless.android.sdk.stats.TestLibraries.Builder
import com.google.wireless.android.sdk.stats.TestLibraries.getDefaultInstance

/**
 * Unit test for [Builder] extension functions for test libraries stats.
 */
class TestSupportLibrariesTest {
  @Test
  fun recordTestLibrary() {
    assertThat(newBuilder().recordTestLibrary("androidx.fragment", "fragment-testing", "1.0").build()).isEqualTo(
      newBuilder().setFragmentTestingVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test", "core", "1.0").build()).isEqualTo(
      newBuilder().setTestCoreVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test", "core-ktx", "1.0").build()).isEqualTo(
      newBuilder().setTestCoreKtxVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test", "orchestrator", "1.0").build()).isEqualTo(
      newBuilder().setTestOrchestratorVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test", "rules", "1.0").build()).isEqualTo(
      newBuilder().setTestRulesVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test", "runner", "1.0").build()).isEqualTo(
      newBuilder().setTestRunnerVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.espresso", "espresso-accessibility", "1.0").build()).isEqualTo(
      newBuilder().setEspressoAccessibilityVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.espresso", "espresso-contrib", "1.0").build()).isEqualTo(
      newBuilder().setEspressoContribVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.espresso", "espresso-core", "1.0").build()).isEqualTo(
      newBuilder().setEspressoVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.espresso", "espresso-idling-resource", "1.0").build()).isEqualTo(
      newBuilder().setEspressoIdlingResourceVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.espresso", "espresso-intents", "1.0").build()).isEqualTo(
      newBuilder().setEspressoIntentsVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.espresso", "espresso-web", "1.0").build()).isEqualTo(
      newBuilder().setEspressoWebVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.espresso", "espresso-device", "1.0").build()).isEqualTo(
      newBuilder().setEspressoDeviceVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.ext", "junit", "1.0").build()).isEqualTo(
      newBuilder().setTestExtJunitVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.ext", "junit-ktx", "1.0").build()).isEqualTo(
      newBuilder().setTestExtJunitKtxVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.test.ext", "truth", "1.0").build()).isEqualTo(
      newBuilder().setTestExtTruthVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test", "orchestrator", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportOrchestratorVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test", "rules", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportRulesVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test", "runner", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportLibraryVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test.espresso", "espresso-accessibility", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportEspressoAccessibilityVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test.espresso", "espresso-contrib", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportEspressoContribVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test.espresso", "espresso-core", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportEspressoVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test.espresso", "espresso-idling-resource", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportEspressoIdlingResourceVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test.espresso", "espresso-intents", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportEspressoIntentsVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.android.support.test.espresso", "espresso-web", "1.0").build()).isEqualTo(
      newBuilder().setTestSupportEspressoWebVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("com.google.truth", "truth", "1.0").build()).isEqualTo(
      newBuilder().setTruthVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("junit", "junit", "1.0").build()).isEqualTo(newBuilder().setJunitVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("org.mockito", "mockito-core", "1.0").build()).isEqualTo(
      newBuilder().setMockitoVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("org.robolectric", "robolectric", "1.0").build()).isEqualTo(
      newBuilder().setRobolectricVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.benchmark", "benchmark-common", "1.0").build()).isEqualTo(
            newBuilder().setBenchmarkCommonVersion("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.benchmark", "benchmark-junit4", "1.0").build()).isEqualTo(
            newBuilder().setBenchmarkJunit4Version("1.0").build())
    assertThat(newBuilder().recordTestLibrary("androidx.benchmark", "benchmark-macro", "1.1.0-alpha02").build()).isEqualTo(
            newBuilder().setBenchmarkMacroVersion("1.1.0-alpha02").build())
    assertThat(newBuilder().recordTestLibrary("androidx.benchmark", "benchmark-macro-junit4", "1.1.0-alpha02").build()).isEqualTo(
            newBuilder().setBenchmarkMacroJunit4Version("1.1.0-alpha02").build())
    assertThat(newBuilder().recordTestLibrary("androidx.compose.ui", "ui-test", "1.2.0-beta01").build()).isEqualTo(
            newBuilder().setComposeUiTestVersion("1.2.0-beta01").build())
    assertThat(newBuilder().recordTestLibrary("androidx.compose.ui", "ui-test-junit4", "1.2.0-beta01").build()).isEqualTo(
            newBuilder().setComposeUiTestJunit4Version("1.2.0-beta01").build())
    assertThat(newBuilder().recordTestLibrary("androidx.compose.ui", "ui-test-manifest", "1.2.0-beta01").build()).isEqualTo(
            newBuilder().setComposeUiTestManifestVersion("1.2.0-beta01").build())
    assertThat(newBuilder().recordTestLibrary("invalid", "name", "1.0").build()).isEqualTo(getDefaultInstance())
  }
}
