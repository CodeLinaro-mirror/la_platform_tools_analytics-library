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

import java.lang.management.ThreadInfo
import java.lang.management.ThreadMXBean
import javax.management.ObjectName
import org.junit.Assert.fail

/** A Stub implementation of [ThreadMXBean] for use in tests. By default fails on any call. */
open class StubThreadBean : ThreadMXBean {

  override fun getThreadCount(): Int {
    fail()
    return 0
  }

  override fun getPeakThreadCount(): Int {
    fail()
    return 0
  }

  override fun getTotalStartedThreadCount(): Long {
    fail()
    return 0
  }

  override fun getDaemonThreadCount(): Int {
    fail()
    return 0
  }

  override fun getAllThreadIds(): LongArray {
    fail()
    return LongArray(0)
  }

  override fun getThreadInfo(id: Long): ThreadInfo? {
    fail()
    return null
  }

  override fun getThreadInfo(ids: LongArray): Array<ThreadInfo> {
    fail()
    return arrayOf()
  }

  override fun getThreadInfo(id: Long, maxDepth: Int): ThreadInfo? {
    fail()
    return null
  }

  override fun getThreadInfo(ids: LongArray, maxDepth: Int): Array<ThreadInfo> {
    fail()
    return arrayOf()
  }

  override fun isThreadContentionMonitoringSupported(): Boolean {
    fail()
    return false
  }

  override fun isThreadContentionMonitoringEnabled(): Boolean {
    fail()
    return false
  }

  override fun setThreadContentionMonitoringEnabled(enable: Boolean) {
    fail()
  }

  override fun getCurrentThreadCpuTime(): Long {
    fail()
    return 0
  }

  override fun getCurrentThreadUserTime(): Long {
    fail()
    return 0
  }

  override fun getThreadCpuTime(id: Long): Long {
    fail()
    return 0
  }

  override fun getThreadUserTime(id: Long): Long {
    fail()
    return 0
  }

  override fun isThreadCpuTimeSupported(): Boolean {
    fail()
    return false
  }

  override fun isCurrentThreadCpuTimeSupported(): Boolean {
    fail()
    return false
  }

  override fun isThreadCpuTimeEnabled(): Boolean {
    fail()
    return false
  }

  override fun setThreadCpuTimeEnabled(enable: Boolean) {
    fail()
  }

  override fun findMonitorDeadlockedThreads(): LongArray {
    fail()
    return LongArray(0)
  }

  override fun resetPeakThreadCount() {
    fail()
  }

  override fun findDeadlockedThreads(): LongArray {
    fail()
    return LongArray(0)
  }

  override fun isObjectMonitorUsageSupported(): Boolean {
    fail()
    return false
  }

  override fun isSynchronizerUsageSupported(): Boolean {
    fail()
    return false
  }

  override fun getThreadInfo(
    ids: LongArray,
    lockedMonitors: Boolean,
    lockedSynchronizers: Boolean,
  ): Array<ThreadInfo> {
    fail()
    return arrayOf()
  }

  override fun dumpAllThreads(
    lockedMonitors: Boolean,
    lockedSynchronizers: Boolean,
  ): Array<ThreadInfo> {
    fail()
    return arrayOf()
  }

  override fun getObjectName(): ObjectName? {
    fail()
    return null
  }
}
