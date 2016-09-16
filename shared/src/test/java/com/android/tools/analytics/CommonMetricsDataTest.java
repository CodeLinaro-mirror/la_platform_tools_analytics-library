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

import static junit.framework.TestCase.assertEquals;

import com.android.testutils.SystemPropertyOverrides;
import com.android.tools.analytics.stubs.StubClassLoadingBean;
import com.android.tools.analytics.stubs.StubGarbageCollectionBean;
import com.android.tools.analytics.stubs.StubGraphicsDevice;
import com.android.tools.analytics.stubs.StubGraphicsEnvironment;
import com.android.tools.analytics.stubs.StubMemoryBean;
import com.android.tools.analytics.stubs.StubOperatingSystemMXBean;
import com.android.tools.analytics.stubs.StubRuntimeMXBean;
import com.android.tools.analytics.stubs.StubThreadBean;
import com.google.common.collect.ImmutableList;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.DeviceInfo.ApplicationBinaryInterface;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.DisplayDetails;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.GarbageCollectionStats;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.JavaProcessStats;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.JvmDetails;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.JvmDetails.GarbageCollector;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.MachineDetails;
import com.google.wireless.android.sdk.stats.AndroidStudioStats.ProductDetails.CpuArchitecture;
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.io.File;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link CommonMetricsData}. */
public class CommonMetricsDataTest {

    public static final String VM_NAME = "VM Name";
    public static final String VM_VENDOR = "VM Vendor";
    public static final String VM_VERSION = "VM Version";
    public static final String FIRST_GC = "FirstGC";
    public static final String SECOND_GC = "SecondGC";

    @Test
    public void cpuArchitectureFromStringTest() {
        assertEquals(
                CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                CommonMetricsData.cpuArchitectureFromString(null));
        assertEquals(
                CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                CommonMetricsData.cpuArchitectureFromString(""));
        assertEquals(CpuArchitecture.X86_64, CommonMetricsData.cpuArchitectureFromString("x86_64"));
        assertEquals(CpuArchitecture.X86_64, CommonMetricsData.cpuArchitectureFromString("ia64"));
        assertEquals(CpuArchitecture.X86_64, CommonMetricsData.cpuArchitectureFromString("amd64"));
        assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("i486"));
        assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("i586"));
        assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("i686"));
        assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("x86"));
        assertEquals(
                CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                CommonMetricsData.cpuArchitectureFromString("x96"));
        assertEquals(
                CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                CommonMetricsData.cpuArchitectureFromString("i6869"));
    }

    @Test
    public void getJvmArchitectureTest() throws Exception {
        try (SystemPropertyOverrides systemPropertyOverrides = new SystemPropertyOverrides()) {
            systemPropertyOverrides.setProperty("os.arch", null);
            assertEquals(
                    CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                    CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "");
            assertEquals(
                    CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                    CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "x86_64");
            assertEquals(CpuArchitecture.X86_64, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "ia64");
            assertEquals(CpuArchitecture.X86_64, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "amd64");
            assertEquals(CpuArchitecture.X86_64, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "i486");
            assertEquals(CpuArchitecture.X86, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "i586");
            assertEquals(CpuArchitecture.X86, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "i686");
            assertEquals(CpuArchitecture.X86, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "x86");
            assertEquals(CpuArchitecture.X86, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "x96");
            assertEquals(
                    CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                    CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "i6869");
            assertEquals(
                    CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                    CommonMetricsData.getJvmArchitecture());
        }
    }

    @Test
    public void getOsArchitectureTest() throws Exception {
        // Override system properties for 'os.arch' and 'os.name'.
        try (SystemPropertyOverrides systemPropertyOverrides = new SystemPropertyOverrides()) {
            systemPropertyOverrides.setProperty("os.arch", null);
            assertEquals(
                    CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                    CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "");
            assertEquals(
                    CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                    CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "x86_64");
            assertEquals(CpuArchitecture.X86_64, CommonMetricsData.getJvmArchitecture());
            systemPropertyOverrides.setProperty("os.arch", "i6869");
            assertEquals(
                    CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE,
                    CommonMetricsData.getJvmArchitecture());

            systemPropertyOverrides.setProperty("os.arch", "x86");
            systemPropertyOverrides.setProperty("os.name", "Windows 10");
            EnvironmentFakes.setSingleProperty("PROCESSOR_ARCHITEW6432", "AMD64");
            assertEquals(CpuArchitecture.X86_64, CommonMetricsData.getOsArchitecture());
            systemPropertyOverrides.setProperty("os.name", "Linux");
            EnvironmentFakes.setSingleProperty("HOSTTYPE", "x86_64");
            assertEquals(CpuArchitecture.X86_64, CommonMetricsData.getOsArchitecture());

        } finally {
            Environment.setInstance(Environment.SYSTEM);
        }
    }

    @Test
    public void getOsNameTest() throws Exception {
        // Override system properties for 'os.name'.
        try (SystemPropertyOverrides systemPropertyOverrides = new SystemPropertyOverrides()) {
            // Test no os specified.
            systemPropertyOverrides.setProperty("os.name", "");
            assertEquals("unknown", CommonMetricsData.getOsName());
            // Test our supported OSes.
            systemPropertyOverrides.setProperty("os.name", "Linux");
            assertEquals("linux", CommonMetricsData.getOsName());
            systemPropertyOverrides.setProperty("os.name", "Windows 10");
            assertEquals("windows", CommonMetricsData.getOsName());
            systemPropertyOverrides.setProperty("os.name", "Windows Vista");
            assertEquals("windows", CommonMetricsData.getOsName());
            systemPropertyOverrides.setProperty("os.name", "Mac OS X");
            assertEquals("macosx", CommonMetricsData.getOsName());
            // Test unknown Oses.
            systemPropertyOverrides.setProperty("os.name", "My Custom OS");
            assertEquals("My Custom OS", CommonMetricsData.getOsName());
            String customLong = "My Custom OS With a really realy long name";
            systemPropertyOverrides.setProperty("os.name", customLong);
            assertEquals(customLong.substring(0, 32), CommonMetricsData.getOsName());
        }
    }

    @Test
    public void getMajorOsVersionTest() throws Exception {
        // Override system properties for 'os.version'.
        try (SystemPropertyOverrides systemPropertyOverrides = new SystemPropertyOverrides()) {
            // Test no version specified.
            systemPropertyOverrides.setProperty("os.version", "3");
            assertEquals(null, CommonMetricsData.getMajorOsVersion());
            // Test supported os version numbers.
            systemPropertyOverrides.setProperty("os.version", "3.13.0-85-generic");
            assertEquals("3.13", CommonMetricsData.getMajorOsVersion());
            systemPropertyOverrides.setProperty("os.version", "10.7.4");
            assertEquals("10.7", CommonMetricsData.getMajorOsVersion());
            systemPropertyOverrides.setProperty("os.version", "10.0");
            assertEquals("10.0", CommonMetricsData.getMajorOsVersion());
            // Test unsupported os version numbers.
            systemPropertyOverrides.setProperty("os.version", "a.b.c");
            assertEquals(null, CommonMetricsData.getMajorOsVersion());
        }
    }

    @Test
    public void applicationBinaryInterfaceFromStringTest() {
        assertEquals(
                ApplicationBinaryInterface.ARME_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString("armeabi"));
        assertEquals(
                ApplicationBinaryInterface.ARME_ABI_V6J,
                CommonMetricsData.applicationBinaryInterfaceFromString("armeabi-v6j"));
        assertEquals(
                ApplicationBinaryInterface.ARME_ABI_V6L,
                CommonMetricsData.applicationBinaryInterfaceFromString("armeabi-v6l"));
        assertEquals(
                ApplicationBinaryInterface.ARME_ABI_V7A,
                CommonMetricsData.applicationBinaryInterfaceFromString("armeabi-v7a"));
        assertEquals(
                ApplicationBinaryInterface.ARM64_V8A_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString("arm64-v8a"));
        assertEquals(
                ApplicationBinaryInterface.MIPS_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString("mips"));
        assertEquals(
                ApplicationBinaryInterface.MIPS_R2_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString("mips-r2"));
        assertEquals(
                ApplicationBinaryInterface.X86_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString("x86"));
        assertEquals(
                ApplicationBinaryInterface.X86_64_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString("x86_64"));
        assertEquals(
                ApplicationBinaryInterface.UNKNOWN_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString(null));
        assertEquals(
                ApplicationBinaryInterface.UNKNOWN_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString(""));
        assertEquals(
                ApplicationBinaryInterface.UNKNOWN_ABI,
                CommonMetricsData.applicationBinaryInterfaceFromString("my_custom_abi"));
    }

    @Test
    public void parseVmOptionSizeTest() {
        // Test various valid values for parsing vm option size in form of:
        // "[0-9]+[GgMmKk]?" to be valid as well as various invalid values.
        assertEquals(CommonMetricsData.EMPTY_SIZE, CommonMetricsData.parseVmOptionSize(""));
        assertEquals(1L, CommonMetricsData.parseVmOptionSize("1"));
        assertEquals(1024L, CommonMetricsData.parseVmOptionSize("1024"));
        assertEquals(1024L, CommonMetricsData.parseVmOptionSize("1k"));
        assertEquals(20480L, CommonMetricsData.parseVmOptionSize("20k"));
        assertEquals(2L * 1024 * 1024, CommonMetricsData.parseVmOptionSize("2M"));
        assertEquals(10L * 1024 * 1024 * 1024, CommonMetricsData.parseVmOptionSize("10G"));
        assertEquals(2L * 1024 * 1024 * 1024 * 1024, CommonMetricsData.parseVmOptionSize("2T"));
        assertEquals(CommonMetricsData.NO_DIGITS, CommonMetricsData.parseVmOptionSize("G"));
        assertEquals(CommonMetricsData.INVALID_POSTFIX, CommonMetricsData.parseVmOptionSize("10Z"));
        assertEquals(
                CommonMetricsData.INVALID_NUMBER,
                CommonMetricsData.parseVmOptionSize(Long.toString(Long.MAX_VALUE) + 0));
    }

    @Test
    public void getJvmDetailsTest() {
        List<String> vmOptions = new ArrayList<>();

        // Stub out the Runtime MX Bean to get consistent naming in test.
        HostData.sRuntimeBean =
                new StubRuntimeMXBean() {
                    @Override
                    public String getVmName() {
                        return VM_NAME;
                    }

                    @Override
                    public String getVmVendor() {
                        return VM_VENDOR;
                    }

                    @Override
                    public String getVmVersion() {
                        return VM_VERSION;
                    }

                    @Override
                    public List<String> getInputArguments() {
                        return vmOptions;
                    }
                };

        try {
            // Test getJvmDetails w/o any VM options specified.
            JvmDetails expectedNoOptions =
                    JvmDetails.newBuilder()
                            .setName(VM_NAME)
                            .setVendor(VM_VENDOR)
                            .setVersion(VM_VERSION)
                            .build();
            JvmDetails resultNoOptions = CommonMetricsData.getJvmDetails();
            assertEquals(expectedNoOptions, resultNoOptions);

            // Test getJvmDetails with the default studio VM options specified.
            vmOptions.add("-server");
            vmOptions.add("-Xms256m");
            vmOptions.add("-Xmx750m");
            vmOptions.add("-XX:MaxPermSize=350m");
            vmOptions.add("-XX:ReservedCodeCacheSize=240m");
            vmOptions.add("-XX:+UseConcMarkSweepGC");
            vmOptions.add("-XX:SoftRefLRUPolicyMSPerMB=50");
            vmOptions.add("-ea");
            vmOptions.add("-XX:-OmitStackTraceInFastThrow");
            vmOptions.add("-Djna.nosys=true");
            vmOptions.add("-Djna.boot.library.path=");
            vmOptions.add("-Djna.debug_load=true");
            vmOptions.add("-Djna.debug_load.jna=true");
            vmOptions.add("-Dsun.io.useCanonCaches=false");
            vmOptions.add("-Djava.net.preferIPv4Stack=true");
            vmOptions.add("-XX:+HeapDumpOnOutOfMemoryError");
            vmOptions.add("-XX:-OmitStackTraceInFastThrow");
            vmOptions.add("-Dawt.useSystemAAFontSettings=lcd");

            JvmDetails expectedAllOptions =
                    JvmDetails.newBuilder()
                            .setName(VM_NAME)
                            .setVendor(VM_VENDOR)
                            .setVersion(VM_VERSION)
                            .setMinimumHeapSize(256L * 1024 * 1024)
                            .setMaximumHeapSize(750L * 1024 * 1024)
                            .setMaximumPermanentSpaceSize(350L * 1024 * 1024)
                            .setMaximumCodeCacheSize(240L * 1024 * 1024)
                            .setSoftReferenceLruPolicy(50L)
                            .setGarbageCollector(GarbageCollector.CONCURRENT_MARK_SWEEP_GC)
                            .build();
            JvmDetails resultAllOptions = CommonMetricsData.getJvmDetails();
            assertEquals(expectedAllOptions, resultAllOptions);

        } finally {
            // undo the stubbing of Runtime MX Bean.
            HostData.sRuntimeBean = null;
        }
    }

    @Test
    public void getMachineDetailsTest() {
        // Use the file root to get a consistent disk size
        // (we normally use the studio install path).
        File root = new File(File.separator);
        // Stub out the Operating System MX Bean to get consistent system info in the test.
        HostData.sOsBean =
                new StubOperatingSystemMXBean() {
                    @Override
                    public int getAvailableProcessors() {
                        return 16;
                    }

                    @Override
                    public long getTotalPhysicalMemorySize() {
                        return 16L * 1024 * 1024 * 1024;
                    }
                };

        // Stub out the Graphics Environment to get consistent screen sizes in the test.
        HostData.sGraphicsEnvironment =
                new StubGraphicsEnvironment() {
                    @Override
                    public GraphicsDevice[] getScreenDevices() throws HeadlessException {
                        return new GraphicsDevice[] {
                            StubGraphicsDevice.withBounds(640, 480),
                            StubGraphicsDevice.withBounds(1024, 768),
                        };
                    }

                    @Override
                    public boolean isHeadlessInstance() {
                        return false;
                    }
                };

        try {
            MachineDetails expected =
                    MachineDetails.newBuilder()
                            .setAvailableProcessors(16)
                            .setTotalRam(16L * 1024 * 1024 * 1024)
                            .setTotalDisk(root.getTotalSpace())
                            .addDisplay(DisplayDetails.newBuilder().setWidth(640).setHeight(480))
                            .addDisplay(DisplayDetails.newBuilder().setWidth(1024).setHeight(768))
                            .build();
            MachineDetails result = CommonMetricsData.getMachineDetails(root);
            Assert.assertEquals(expected, result);
        } finally {
            // undo the stubbing of Operating System MX Bean.
            HostData.sOsBean = null;
            // undo the stubbing of Graphics Environment.
            HostData.sGraphicsEnvironment = null;
        }
    }

    @Test
    public void getGarbageCollectionStatsTest() {
        try {
            // Stub out the Garbage Collector MX Bean for consistent GC stats in this test.
            HostData.sGarbageCollectorBeans =
                    ImmutableList.of(
                            StubGarbageCollectionBean.fixedValue(FIRST_GC, 100, 123),
                            StubGarbageCollectionBean.fixedValue(SECOND_GC, 404, 512));

            GarbageCollectionStats firstExpected =
                    GarbageCollectionStats.newBuilder()
                            .setName(FIRST_GC)
                            .setGcCollections(100)
                            .setGcTime(123)
                            .build();

            GarbageCollectionStats secondExpected =
                    GarbageCollectionStats.newBuilder()
                            .setName(SECOND_GC)
                            .setGcCollections(404)
                            .setGcTime(512)
                            .build();

            List<GarbageCollectionStats> results1 = CommonMetricsData.getGarbageCollectionStats();
            Assert.assertEquals(2, results1.size());

            assertEquals(firstExpected, results1.get(0));
            assertEquals(secondExpected, results1.get(1));

            // Update the Garbage Collector MX Beans Stub with new values.
            HostData.sGarbageCollectorBeans =
                    ImmutableList.of(
                            StubGarbageCollectionBean.fixedValue(FIRST_GC, 200, 234),
                            StubGarbageCollectionBean.fixedValue(SECOND_GC, 501, 1024));

            // We expect results to be a diff instead of commulative of above values.
            GarbageCollectionStats thirdExpected =
                    GarbageCollectionStats.newBuilder()
                            .setName(FIRST_GC)
                            .setGcCollections(100)
                            .setGcTime(111)
                            .build();

            GarbageCollectionStats fourthExpected =
                    GarbageCollectionStats.newBuilder()
                            .setName(SECOND_GC)
                            .setGcCollections(97)
                            .setGcTime(512)
                            .build();

            List<GarbageCollectionStats> results2 = CommonMetricsData.getGarbageCollectionStats();
            Assert.assertEquals(2, results2.size());

            assertEquals(thirdExpected, results2.get(0));
            assertEquals(fourthExpected, results2.get(1));

        } finally {
            // undo the stubbing of Garbage Collector MX Beans.
            HostData.sGarbageCollectorBeans = null;
            // Reset our collection stats for future tests.
            CommonMetricsData.sGarbageCollectionStats.clear();
        }
    }

    @Test
    public void getJavaProcessStatsTest() {
        try {
            // Stub out the Garbage Collector MX Bean for consistent GC stats in this test.
            HostData.sGarbageCollectorBeans =
                    ImmutableList.of(
                            StubGarbageCollectionBean.fixedValue(FIRST_GC, 100, 123),
                            StubGarbageCollectionBean.fixedValue(SECOND_GC, 404, 512));

            // Stub out the Memory MX Bean for consistent memory stats in this test.
            HostData.sMemoryBean =
                    new StubMemoryBean() {
                        @Override
                        public MemoryUsage getHeapMemoryUsage() {
                            return new MemoryUsage(1, 2, 3, 4);
                        }

                        @Override
                        public MemoryUsage getNonHeapMemoryUsage() {
                            return new MemoryUsage(5, 6, 7, 8);
                        }
                    };

            // Stub out the Class Loading MX Bean for consistent class stats in this test.
            HostData.sClassLoadingBean =
                    new StubClassLoadingBean() {
                        @Override
                        public int getLoadedClassCount() {
                            return 100;
                        }
                    };

            HostData.sThreadBean =
                    new StubThreadBean() {
                        @Override
                        public int getThreadCount() {
                            return 5;
                        }
                    };

            JavaProcessStats expected =
                    JavaProcessStats.newBuilder()
                            .setHeapMemoryUsage(2)
                            .setNonHeapMemoryUsage(6)
                            .setLoadedClassCount(100)
                            .addGarbageCollectionStats(
                                    GarbageCollectionStats.newBuilder()
                                            .setName(FIRST_GC)
                                            .setGcCollections(100)
                                            .setGcTime(123)
                                            .build())
                            .addGarbageCollectionStats(
                                    GarbageCollectionStats.newBuilder()
                                            .setName(SECOND_GC)
                                            .setGcCollections(404)
                                            .setGcTime(512)
                                            .build())
                            .setThreadCount(5)
                            .build();

            JavaProcessStats result = CommonMetricsData.getJavaProcessStats();
            Assert.assertEquals(expected, result);
        } finally {
            // undo the stubbing of the various MX Beans.
            HostData.sGarbageCollectorBeans = null;
            HostData.sMemoryBean = null;
            HostData.sClassLoadingBean = null;
            // Reset our collection stats for future tests.
            CommonMetricsData.sGarbageCollectionStats.clear();
        }
    }
}
