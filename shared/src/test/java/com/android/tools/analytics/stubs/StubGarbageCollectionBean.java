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

import java.lang.management.GarbageCollectorMXBean;
import javax.management.ObjectName;

/**
 * A Stub implementation of {@link GarbageCollectorMXBean} for use in tests. By default fails on any
 * call.
 */
public class StubGarbageCollectionBean implements GarbageCollectorMXBean {

    @Override
    public long getCollectionCount() {
        fail();
        return 0;
    }

    @Override
    public long getCollectionTime() {
        fail();
        return 0;
    }

    @Override
    public String getName() {
        fail();
        return null;
    }

    @Override
    public boolean isValid() {
        fail();
        return false;
    }

    @Override
    public String[] getMemoryPoolNames() {
        fail();
        return new String[0];
    }

    @Override
    public ObjectName getObjectName() {
        fail();
        return null;
    }

    /** Creates a Stub {@link GarbageCollectorMXBean} using fixed values for provided arguments. */
    public static GarbageCollectorMXBean fixedValue(String name, long collections, long time) {
        return new StubGarbageCollectionBean() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public long getCollectionCount() {
                return collections;
            }

            @Override
            public long getCollectionTime() {
                return time;
            }
        };
    }
}
