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

@file:JvmName("TestSupportLibraries")
package com.android.tools.analytics

import com.google.wireless.android.sdk.stats.TestLibraries

/*
 * Fills in the right field of a [TestLibraries.Builder] based on the maven coordinates.
 */
fun TestLibraries.Builder.recordTestLibrary(groupId: String, artifactId: String, version: String) {
    when (groupId) {
        "com.android.support.test", "androidx.test" -> {
            when (artifactId) {
                "orchestrator" -> testOrchestratorVersion = version
                "rules" -> testRulesVersion = version
                "runner" -> testSupportLibraryVersion = version
            }
        }
        "com.android.support.test.espresso", "androidx.test.espresso" -> {
            when (artifactId) {
                "espresso-accessibility" -> espressoAccessibilityVersion = version
                "espresso-contrib" -> espressoContribVersion = version
                "espresso-core" -> espressoVersion = version
                "espresso-idling-resource" -> espressoIdlingResourceVersion = version
                "espresso-intents" -> espressoIntentsVersion = version
                "espresso-web" -> espressoWebVersion = version
            }
        }
        "org.robolectric" -> {
            when (artifactId) {
                "robolectric" -> robolectricVersion = version
            }
        }
        "org.mockito" -> {
            when (artifactId) {
                "mockito-core" -> mockitoVersion = version
            }
        }
    }
}