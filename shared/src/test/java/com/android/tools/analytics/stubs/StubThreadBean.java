/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.tools.analytics.stubs;

import static org.junit.Assert.fail;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import javax.management.ObjectName;

/**
 * A Stub implementation of {@link ThreadMXBean} for use in tests. By default fails on any
 * call.
 */
public class StubThreadBean implements ThreadMXBean {

    @Override
    public int getThreadCount() {
        fail();
        return 0;
    }

    @Override
    public int getPeakThreadCount() {
        fail();
        return 0;
    }

    @Override
    public long getTotalStartedThreadCount() {
        fail();
        return 0;
    }

    @Override
    public int getDaemonThreadCount() {
        fail();
        return 0;
    }

    @Override
    public long[] getAllThreadIds() {
        fail();
        return new long[0];
    }

    @Override
    public ThreadInfo getThreadInfo(long id) {
        fail();
        return null;
    }

    @Override
    public ThreadInfo[] getThreadInfo(long[] ids) {
        fail();
        return new ThreadInfo[0];
    }

    @Override
    public ThreadInfo getThreadInfo(long id, int maxDepth) {
        fail();
        return null;
    }

    @Override
    public ThreadInfo[] getThreadInfo(long[] ids, int maxDepth) {
        fail();
        return new ThreadInfo[0];
    }

    @Override
    public boolean isThreadContentionMonitoringSupported() {
        fail();
        return false;
    }

    @Override
    public boolean isThreadContentionMonitoringEnabled() {
        fail();
        return false;
    }

    @Override
    public void setThreadContentionMonitoringEnabled(boolean enable) {
        fail();
    }

    @Override
    public long getCurrentThreadCpuTime() {
        fail();
        return 0;
    }

    @Override
    public long getCurrentThreadUserTime() {
        fail();
        return 0;
    }

    @Override
    public long getThreadCpuTime(long id) {
        fail();
        return 0;
    }

    @Override
    public long getThreadUserTime(long id) {
        fail();
        return 0;
    }

    @Override
    public boolean isThreadCpuTimeSupported() {
        fail();
        return false;
    }

    @Override
    public boolean isCurrentThreadCpuTimeSupported() {
        fail();
        return false;
    }

    @Override
    public boolean isThreadCpuTimeEnabled() {
        fail();
        return false;
    }

    @Override
    public void setThreadCpuTimeEnabled(boolean enable) {
        fail();
    }

    @Override
    public long[] findMonitorDeadlockedThreads() {
        fail();
        return new long[0];
    }

    @Override
    public void resetPeakThreadCount() {
        fail();
    }

    @Override
    public long[] findDeadlockedThreads() {
        fail();
        return new long[0];
    }

    @Override
    public boolean isObjectMonitorUsageSupported() {
        fail();
        return false;
    }

    @Override
    public boolean isSynchronizerUsageSupported() {
        fail();
        return false;
    }

    @Override
    public ThreadInfo[] getThreadInfo(
            long[] ids, boolean lockedMonitors, boolean lockedSynchronizers) {
        fail();
        return new ThreadInfo[0];
    }

    @Override
    public ThreadInfo[] dumpAllThreads(boolean lockedMonitors, boolean lockedSynchronizers) {
        fail();
        return new ThreadInfo[0];
    }

    @Override
    public ObjectName getObjectName() {
        fail();
        return null;
    }
}
