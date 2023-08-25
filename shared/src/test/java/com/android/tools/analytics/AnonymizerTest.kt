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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Tests for @{link Anonymizer}. */
class AnonymizerTest {

  @get:Rule val testConfigDir = TemporaryFolder()

  @Test
  fun uninitialized() {
    AnalyticsSettings.setInstanceForTest(null)

    assertNull(Anonymizer.anonymize("abcd"))
  }

  @Test
  fun anonymizerTest() {
    // Configure the paths to use a temp directory for reading from and writing to.
    EnvironmentFakes.setCustomAndroidPrefsRootEnvironment(testConfigDir.root.toPath().toString())
    try {
      // Prepopulate AnalysisSettings.
      AnalyticsSettings.setInstanceForTest(AnalyticsSettingsData())

      // Set date to a specific skew range.
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 3, 18)

      // Ensure we get some form of anonymization.
      val data1 = Anonymizer.anonymize(MY_RANDOM_TEXT1)
      assertNotNull(data1)
      assertNotEquals(MY_RANDOM_TEXT1, data1)

      // Ensure different input gives different output.
      val other = Anonymizer.anonymize(MY_RANDOM_TEXT2)
      assertNotEquals(data1, other)

      // Ensure that anonymizing is stable with time stable.
      val data2 = Anonymizer.anonymize(MY_RANDOM_TEXT1)
      assertEquals(data1, data2)

      // Set date to different date in same skew range.
      AnalyticsSettings.dateProvider = StubDateProvider(2016, 4, 15)
      val data3 = Anonymizer.anonymize(MY_RANDOM_TEXT1)
      // Ensure that same input is stable for dates in same skew range.
      assertEquals(data1, data3)

      // Set date to new skew range
      AnalyticsSettings.dateProvider = StubDateProvider(2019, 4, 16)

      // Ensure that same input is different for different skew range.
      val data4 = Anonymizer.anonymize(MY_RANDOM_TEXT1)
      assertNotEquals(data1, data4)

      // Ensure that null and empty are reported as empty.
      val data6 = Anonymizer.anonymize(null)
      assertEquals("", data6)
      val data7 = Anonymizer.anonymize("")
      assertEquals("", data7)
    } finally {
      // Undo stub of DateProvider.
      AnalyticsSettings.dateProvider = DateProvider.SYSTEM
      EnvironmentFakes.setSystemEnvironment()
    }
  }

  companion object {
    private const val MY_RANDOM_TEXT1 = "My random text"
    private const val MY_RANDOM_TEXT2 = "More random text"
  }
}
