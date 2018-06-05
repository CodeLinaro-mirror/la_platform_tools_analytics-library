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

import java.lang.management.MemoryMXBean
import java.lang.management.MemoryUsage
import javax.management.ObjectName

/** A Stub implementation of [MemoryMXBean] for use in tests. By default fails on any call.  */
open class StubMemoryBean : MemoryMXBean {

  override fun getObjectPendingFinalizationCount(): Int {
    fail()
    return 0
  }

  override fun getHeapMemoryUsage(): MemoryUsage? {
    fail()
    return null
  }

  override fun getNonHeapMemoryUsage(): MemoryUsage? {
    fail()
    return null
  }

  override fun isVerbose(): Boolean {
    fail()
    return false
  }

  override fun setVerbose(value: Boolean) {
    fail()
  }

  override fun gc() {
    fail()
  }

  override fun getObjectName(): ObjectName? {
    fail()
    return null
  }
}
