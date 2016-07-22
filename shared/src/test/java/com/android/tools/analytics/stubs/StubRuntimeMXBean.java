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

import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;

/**
 * A Stub implementation of {@link RuntimeMXBean} for use in tests. By default fails on any call.
 */
public class StubRuntimeMXBean implements RuntimeMXBean {
    @Override
    public String getName() {
        fail();
        return null;
    }

    @Override
    public String getVmName() {
        fail();
        return null;
    }

    @Override
    public String getVmVendor() {
        fail();
        return null;
    }

    @Override
    public String getVmVersion() {
        fail();
        return null;
    }

    @Override
    public String getSpecName() {
        fail();
        return null;
    }

    @Override
    public String getSpecVendor() {
        fail();
        return null;
    }

    @Override
    public String getSpecVersion() {
        fail();
        return null;
    }

    @Override
    public String getManagementSpecVersion() {
        fail();
        return null;
    }

    @Override
    public String getClassPath() {
        fail();
        return null;
    }

    @Override
    public String getLibraryPath() {
        fail();
        return null;
    }

    @Override
    public boolean isBootClassPathSupported() {
        fail();
        return false;
    }

    @Override
    public String getBootClassPath() {
        fail();
        return null;
    }

    @Override
    public List<String> getInputArguments() {
        fail();
        return null;
    }

    @Override
    public long getUptime() {
        fail();
        return 0;
    }

    @Override
    public long getStartTime() {
        fail();
        return 0;
    }

    @Override
    public Map<String, String> getSystemProperties() {
        fail();
        return null;
    }

    @Override
    public ObjectName getObjectName() {
        fail();
        return null;
    }
}
