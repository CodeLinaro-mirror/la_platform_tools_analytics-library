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

import com.android.tools.analytics.stubs.StubDateWebServer
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.util.*
import org.junit.Test

const val MILLI_TO_NANOS = 1_000_000

/** Tests for @{link Anonymizer}. */
class WebServerDateProviderTest {
  @Test
  fun connectedTest() {
    var offset = 10_000L
    val stub = StubDateWebServer()
    try {
      val start = Date(Date.parse("Fri, 03 Aug 2018 19:59:10 GMT"))
      stub.replyForDate(start)
      val dateProvider =
        object : WebServerDateProvider(stub.url) {
          override fun nanoTime() = offset
        }

      var now = dateProvider.now()
      assertThat(now.time).isEqualTo(start.time + (offset / MILLI_TO_NANOS))
      offset = 30_000L
      now = dateProvider.now()
      assertThat(now.time).isEqualTo(start.time + (offset / MILLI_TO_NANOS))

      val skewed = Date(Date.parse("Fri, 03 Aug 2018 22:59:10 GMT"))
      stub.replyForDate(skewed)
      assertThat(dateProvider.updateServerTimestampWithHeadRequest(stub.url)).isTrue()

      now = dateProvider.now()
      assertThat(now.time).isEqualTo(skewed.time + (offset / MILLI_TO_NANOS))

      stub.replyFreeFormDate("Dinsdag 31 Juli 2018")
      assertThat(dateProvider.updateServerTimestampWithHeadRequest(stub.url)).isFalse()

      now = dateProvider.now()
      assertThat(now.time).isEqualTo(skewed.time + (offset / MILLI_TO_NANOS))

      stub.replyNoDate()
      assertThat(dateProvider.updateServerTimestampWithHeadRequest(stub.url)).isFalse()

      now = dateProvider.now()
      assertThat(now.time).isEqualTo(skewed.time + (offset / MILLI_TO_NANOS))
    } finally {
      stub.close()
    }
  }

  @Test
  fun noConnection() {
    try {
      val stub = StubDateWebServer()
      stub.close()
      WebServerDateProvider(stub.url)
      throw RuntimeException("WebServer call should have failed")
    } catch (_: IOException) {
      // expected
    }
  }
}
