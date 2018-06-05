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

import org.junit.Assert.fail

import java.lang.management.GarbageCollectorMXBean
import javax.management.ObjectName

/**
 * A Stub implementation of [GarbageCollectorMXBean] for use in tests. By default fails on any
 * call.
 */
open class StubGarbageCollectionBean : GarbageCollectorMXBean {

  override fun getCollectionCount(): Long {
    fail()
    return 0
  }

  override fun getCollectionTime(): Long {
    fail()
    return 0
  }

  override fun getName(): String? {
    fail()
    return null
  }

  override fun isValid(): Boolean {
    fail()
    return false
  }

  override fun getMemoryPoolNames(): Array<String> {
    fail()
    return arrayOf()
  }

  override fun getObjectName(): ObjectName? {
    fail()
    return null
  }

  companion object {

    @JvmStatic
      /** Creates a Stub [GarbageCollectorMXBean] using fixed values for provided arguments.  */
    fun fixedValue(name: String, collections: Long, time: Long): GarbageCollectorMXBean {
      return object : StubGarbageCollectionBean() {
        override fun getName(): String? {
          return name
        }

        override fun getCollectionCount(): Long {
          return collections
        }

        override fun getCollectionTime(): Long {
          return time
        }
      }
    }
  }
}
