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

import java.lang.management.RuntimeMXBean
import javax.management.ObjectName
import org.junit.Assert.fail

/** A Stub implementation of [RuntimeMXBean] for use in tests. By default fails on any call. */
open class StubRuntimeMXBean : RuntimeMXBean {
  override fun getName(): String? {
    fail()
    return null
  }

  override fun getVmName(): String? {
    fail()
    return null
  }

  override fun getVmVendor(): String? {
    fail()
    return null
  }

  override fun getVmVersion(): String? {
    fail()
    return null
  }

  override fun getSpecName(): String? {
    fail()
    return null
  }

  override fun getSpecVendor(): String? {
    fail()
    return null
  }

  override fun getSpecVersion(): String? {
    fail()
    return null
  }

  override fun getManagementSpecVersion(): String? {
    fail()
    return null
  }

  override fun getClassPath(): String? {
    fail()
    return null
  }

  override fun getLibraryPath(): String? {
    fail()
    return null
  }

  override fun isBootClassPathSupported(): Boolean {
    fail()
    return false
  }

  override fun getBootClassPath(): String? {
    fail()
    return null
  }

  override fun getInputArguments(): List<String>? {
    fail()
    return null
  }

  override fun getUptime(): Long {
    fail()
    return 0
  }

  override fun getStartTime(): Long {
    fail()
    return 0
  }

  override fun getSystemProperties(): Map<String, String>? {
    fail()
    return null
  }

  override fun getObjectName(): ObjectName? {
    fail()
    return null
  }
}
