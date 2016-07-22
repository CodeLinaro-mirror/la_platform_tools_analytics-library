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

import java.lang.management.ClassLoadingMXBean;
import javax.management.ObjectName;

/**
 * A Stub implementation of {@link ClassLoadingMXBean} for use in tests. By default fails on any
 * call.
 */
public class StubClassLoadingBean implements ClassLoadingMXBean {

    @Override
    public long getTotalLoadedClassCount() {
        fail();
        return 0;
    }

    @Override
    public int getLoadedClassCount() {
        fail();
        return 0;
    }

    @Override
    public long getUnloadedClassCount() {
        fail();
        return 0;
    }

    @Override
    public boolean isVerbose() {
        fail();
        return false;
    }

    @Override
    public void setVerbose(boolean value) {
        fail();
    }

    @Override
    public ObjectName getObjectName() {
        fail();
        return null;
    }
}
