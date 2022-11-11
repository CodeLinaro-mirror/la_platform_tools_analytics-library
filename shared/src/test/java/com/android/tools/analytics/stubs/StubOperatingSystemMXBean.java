/*
 * Copyright (C) 2022 The Android Open Source Project
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

import com.sun.management.OperatingSystemMXBean;

import javax.management.ObjectName;

/**
 * A Stub implementation of [OperatingSystemMXBean] for use in tests. By default fails on any call.
 */
public class StubOperatingSystemMXBean implements OperatingSystemMXBean {

    @Override
    public String getName() {
        fail();
        return null;
    }

    @Override
    public String getArch() {
        fail();
        return null;
    }

    @Override
    public String getVersion() {
        fail();
        return null;
    }

    @Override
    public int getAvailableProcessors() {
        fail();
        return 0;
    }

    @Override
    public double getSystemLoadAverage() {
        fail();
        return 0;
    }

    @Override
    public ObjectName getObjectName() {
        fail();
        return null;
    }

    @Override
    public long getCommittedVirtualMemorySize() {
        fail();
        return 0;
    }

    @Override
    public long getTotalSwapSpaceSize() {
        fail();
        return 0;
    }

    @Override
    public long getFreeSwapSpaceSize() {
        fail();
        return 0;
    }

    @Override
    public long getProcessCpuTime() {
        fail();
        return 0;
    }

    @Override
    public long getFreePhysicalMemorySize() {
        fail();
        return 0;
    }

    @Override
    public long getTotalPhysicalMemorySize() {
        fail();
        return 0;
    }

    @Override
    public double getSystemCpuLoad() {
        fail();
        return 0;
    }

    @Override
    public double getProcessCpuLoad() {
        fail();
        return 0;
    }

    //TODO add @Override annotation once migrated to java17
    public long getFreeMemorySize() {
        fail();
        return 0;
    }

    //TODO add @Override annotation once migrated to java17
    public long getTotalMemorySize() {
        fail();
        return 0;
    }

    //TODO add @Override annotation once migrated to java17
    public double getCpuLoad() {
        fail();
        return 0;
    }
}
