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
package com.android.tools.analytics;

import com.android.annotations.VisibleForTesting;
import com.sun.management.OperatingSystemMXBean;
import java.awt.GraphicsEnvironment;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * Entry point to various host data classes such as MxBeans and GraphicsEnvironment. Used to allow
 * stubbing these out in tests.
 */
@VisibleForTesting
class HostData {

    @VisibleForTesting static OperatingSystemMXBean sOsBean;
    @VisibleForTesting static RuntimeMXBean sRuntimeBean;
    @VisibleForTesting static GraphicsEnvironment sGraphicsEnvironment;
    @VisibleForTesting static MemoryMXBean sMemoryBean;
    @VisibleForTesting static ClassLoadingMXBean sClassLoadingBean;
    @VisibleForTesting static List<GarbageCollectorMXBean> sGarbageCollectorBeans;
    @VisibleForTesting static ThreadMXBean sThreadBean;

    public static OperatingSystemMXBean getOsBean() {
        if (sOsBean == null) {
            sOsBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        }
        return sOsBean;
    }

    public static RuntimeMXBean getRuntimeBean() {
        if (sRuntimeBean == null) {
            sRuntimeBean = ManagementFactory.getRuntimeMXBean();
        }
        return sRuntimeBean;
    }

    public static GraphicsEnvironment getGraphicsEnvironment() {
        if (sGraphicsEnvironment == null) {
            sGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        }
        return sGraphicsEnvironment;
    }

    public static MemoryMXBean getMemoryBean() {
        if (sMemoryBean == null) {
            sMemoryBean = ManagementFactory.getMemoryMXBean();
        }
        return sMemoryBean;
    }

    public static ClassLoadingMXBean getClassLoadingBean() {
        if (sClassLoadingBean == null) {
            sClassLoadingBean = ManagementFactory.getClassLoadingMXBean();
        }
        return sClassLoadingBean;
    }

    public static List<GarbageCollectorMXBean> getGarbageCollectorBeans() {
        if (sGarbageCollectorBeans == null) {
            sGarbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();
        }
        return sGarbageCollectorBeans;
    }

    public static ThreadMXBean getThreadBean() {
        if (sThreadBean == null) {
            sThreadBean = ManagementFactory.getThreadMXBean();
        }
        return sThreadBean;
    }

}
