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
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors

class UsageTrackerInitializationTest {

    private val analyticsSettingsData = AnalyticsSettingsData()
    private val scheduledExecutorService = Executors.newScheduledThreadPool(1)

    @Before
    fun setUp() {
        analyticsSettingsData.optedIn = true
        AnalyticsSettings.setInstanceForTest(analyticsSettingsData)
        UsageTracker.deinitialize()
    }

    @Test
    fun testInitializeFunction() {
        UsageTracker.initialize(scheduledExecutorService)
        val writer = UsageTracker.writer
        Truth.assertThat(writer).isNotInstanceOf(NullUsageTracker.javaClass)

        UsageTracker.initialize(scheduledExecutorService)
        Truth.assertThat(writer).isNotInstanceOf(NullUsageTracker.javaClass)
        Truth.assertThat(UsageTracker.writer).isNotEqualTo(writer)

        // initialize function allows us to re-initialize UsageTrackerWriter when optedIn changes
        analyticsSettingsData.optedIn = false
        UsageTracker.initialize(scheduledExecutorService)
        Truth.assertThat(UsageTracker.writer).isInstanceOf(NullUsageTracker.javaClass)
    }

    @Test
    fun testInitializeIfNotPresentFunction() {
        UsageTracker.initializeIfNotPresent(scheduledExecutorService)
        val writer = UsageTracker.writer
        Truth.assertThat(writer).isNotInstanceOf(NullUsageTracker.javaClass)

        // If UsageTracker is initialized, UsageTrackerWriter instance won't change when
        // initializeIfNotPresent function is invoked
        UsageTracker.initializeIfNotPresent(scheduledExecutorService)
        Truth.assertThat(UsageTracker.writer).isEqualTo(writer)

        analyticsSettingsData.optedIn = false
        UsageTracker.initializeIfNotPresent(scheduledExecutorService)
        Truth.assertThat(UsageTracker.writer).isEqualTo(writer)
    }
}
