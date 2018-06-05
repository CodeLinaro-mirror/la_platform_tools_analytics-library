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

package com.android.tools.analytics.stubs

import com.android.utils.DateProvider
import java.time.ZoneOffset
import java.util.*

/**
 * A [DateProvider] that can be set to a specific date for use in tests. NOTE: months are 0-11
 * while days are 1-31 range.
 *
 * Uses UTC as time zone.
 */
class StubDateProvider(private val year: Int, private val month: Int, private val day: Int) : DateProvider {

  override fun now(): Date {
    val calendar = GregorianCalendar(year, month, day)
    calendar.timeZone = TimeZone.getTimeZone(ZoneOffset.UTC)
    return calendar.time
  }
}
