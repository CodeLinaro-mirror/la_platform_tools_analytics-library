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

import com.android.annotations.NonNull;
import com.android.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.wireless.android.sdk.stats.DeviceInfo.ApplicationBinaryInterface;
import com.google.wireless.android.sdk.stats.DisplayDetails;
import com.google.wireless.android.sdk.stats.GarbageCollectionStats;
import com.google.wireless.android.sdk.stats.JavaProcessStats;
import com.google.wireless.android.sdk.stats.JvmDetails;
import com.google.wireless.android.sdk.stats.MachineDetails;
import com.google.wireless.android.sdk.stats.ProductDetails;
import com.sun.management.OperatingSystemMXBean;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Calculates common pieces of metrics data, used in various Android DevTools. */
public class CommonMetricsData {

    public static final String VM_OPTION_XMS = "-Xms";
    public static final String VM_OPTION_XMX = "-Xmx";
    public static final String VM_OPTION_MAX_PERM_SIZE = "-XX:MaxPermSize=";
    public static final String VM_OPTION_RESERVED_CODE_CACHE_SIZE = "-XX:ReservedCodeCacheSize=";
    public static final String VM_OPTION_SOFT_REF_LRU_POLICY_MS_PER_MB =
            "-XX:SoftRefLRUPolicyMSPerMB=";
    public static final long KILOBYTE = 1024L;
    public static final long MEGABYTE = KILOBYTE * 1024;
    public static final long GIGABYTE = MEGABYTE * 1024;
    public static final long TERABYTE = GIGABYTE * 1024;
    public static final int NO_DIGITS = -1;
    public static final int INVALID_POSTFIX = -2;
    public static final int INVALID_NUMBER = -3;
    public static final int EMPTY_SIZE = -4;

    /** Used to calculate diffs between different reports of Garbage Collection stats. */
    @VisibleForTesting
    static class GarbageCollectionStatsDiffs {
        volatile long collections;
        volatile long time;
    }

    @VisibleForTesting
    static final Map<String, GarbageCollectionStatsDiffs> sGarbageCollectionStats = new HashMap<>();

    /**
     * Detects and returns the OS architecture: x86, x86_64, ppc. This may differ or be equal to the
     * JVM architecture in the sense that a 64-bit OS can run a 32-bit JVM.
     */
    public static ProductDetails.CpuArchitecture getOsArchitecture() {
        ProductDetails.CpuArchitecture jvmArchitecture = getJvmArchitecture();
        if (jvmArchitecture == ProductDetails.CpuArchitecture.X86) {
            // This is the misleading case: the JVM is 32-bit but the OS
            // might be either 32 or 64. We can't tell just from this
            // property.
            // Macs are always on 64-bit, so we just need to figure it
            // out for Windows and Linux.

            String os = System.getProperty("os.name").toLowerCase();
            if (os.startsWith("win")) { //$NON-NLS-1$
                // When WOW64 emulates a 32-bit environment under a 64-bit OS,
                // it sets PROCESSOR_ARCHITEW6432 to AMD64 or IA64 accordingly.
                // Ref: http://msdn.microsoft.com/en-us/library/aa384274(v=vs.85).aspx

                String w6432 = Environment.getInstance().getVariable("PROCESSOR_ARCHITEW6432");
                if (w6432 != null && w6432.contains("64")) {
                    return ProductDetails.CpuArchitecture.X86_64;
                }
            } else if (os.startsWith("linux")) {
                // Let's try the obvious. This works in Ubuntu and Debian
                String s = Environment.getInstance().getVariable("HOSTTYPE");
                return cpuArchitectureFromString(s);
            }
        }
        return jvmArchitecture;
    }

    /**
     * Gets the JVM Architecture, NOTE this might not be the same as OS architecture. See {@link
     * #getOsArchitecture()} if OS architecture is needed.
     */
    public static ProductDetails.CpuArchitecture getJvmArchitecture() {
        String arch = System.getProperty("os.arch");
        return cpuArchitectureFromString(arch);
    }

    /**
     * Builds a {@link ProductDetails.CpuArchitecture} instance based on the provided string (e.g.
     * "x86_64").
     */
    public static ProductDetails.CpuArchitecture cpuArchitectureFromString(String cpuArchitecture) {
        if (cpuArchitecture == null || cpuArchitecture.length() == 0) {
            return ProductDetails.CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE;
        }

        if (cpuArchitecture.equalsIgnoreCase("x86_64")
                || cpuArchitecture.equalsIgnoreCase("ia64")
                || cpuArchitecture.equalsIgnoreCase("amd64")) {
            return ProductDetails.CpuArchitecture.X86_64;
        }

        if (cpuArchitecture.equalsIgnoreCase("x86")) {
            return ProductDetails.CpuArchitecture.X86;
        }

        if (cpuArchitecture.length() == 4
                && cpuArchitecture.charAt(0) == 'i'
                && cpuArchitecture.indexOf("86") == 2) {
            // Any variation of iX86 counts as x86 (i386, i486, i686).
            return ProductDetails.CpuArchitecture.X86;
        }
        return ProductDetails.CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE;
    }

    /** Gets a normalized version of the os name that this code is running on. */
    public static String getOsName() {
        String os = System.getProperty("os.name");

        if (os == null || os.length() == 0) {
            return "unknown";
        }

        String osLower = os.toLowerCase(Locale.US);

        if (osLower.startsWith("mac")) {
            os = "macosx";
        } else if (osLower.startsWith("win")) { //$NON-NLS-1$
            os = "windows";
        } else if (osLower.startsWith("linux")) { //$NON-NLS-1$
            os = "linux";

        } else if (os.length() > 32) {
            // Unknown -- send it verbatim so we can see it
            // but protect against arbitrarily long values
            os = os.substring(0, 32);
        }
        return os;
    }

    /**
     * Extracts the major os version that this code is running on in the form of '[0-9]+\.[0-9]+'
     */
    public static String getMajorOsVersion() {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+).*");
        String osVers = System.getProperty("os.version");
        if (osVers != null && osVers.length() > 0) {
            Matcher m = p.matcher(osVers);
            if (m.matches()) {
                return m.group(1) + '.' + m.group(2);
            }
        }
        return null;
    }

    public static ApplicationBinaryInterface applicationBinaryInterfaceFromString(String value) {
        if (value == null) {
            return ApplicationBinaryInterface.UNKNOWN_ABI;
        }
        switch (value) {
            case "armeabi-v6j":
                return ApplicationBinaryInterface.ARME_ABI_V6J;
            case "armeabi-v6l":
                return ApplicationBinaryInterface.ARME_ABI_V6L;
            case "armeabi-v7a":
                return ApplicationBinaryInterface.ARME_ABI_V7A;
            case "armeabi":
                return ApplicationBinaryInterface.ARME_ABI;
            case "arm64-v8a":
                return ApplicationBinaryInterface.ARM64_V8A_ABI;
            case "mips":
                return ApplicationBinaryInterface.MIPS_ABI;
            case "mips-r2":
                return ApplicationBinaryInterface.MIPS_R2_ABI;
            case "x86":
                return ApplicationBinaryInterface.X86_ABI;
            case "x86_64":
                return ApplicationBinaryInterface.X86_64_ABI;
            default:
                return ApplicationBinaryInterface.UNKNOWN_ABI;
        }
    }

    /**
     * Gets details about the machine this code is running on.
     *
     * @param homePath path to use to track total disk space.
     */
    @NonNull
    public static MachineDetails getMachineDetails(@NonNull File homePath) {
        OperatingSystemMXBean osBean = HostData.getOsBean();

        return MachineDetails.newBuilder()
                .setAvailableProcessors(osBean.getAvailableProcessors())
                .setTotalRam(osBean.getTotalPhysicalMemorySize())
                .setTotalDisk(homePath.getTotalSpace())
                .addAllDisplay(getDisplayDetails())
                .build();
    }

    /** Gets information about all the displays connected to this machine. */
    @NonNull
    private static Iterable<? extends DisplayDetails> getDisplayDetails() {
        List<DisplayDetails> displays = new ArrayList<>();

        GraphicsEnvironment graphics = HostData.getGraphicsEnvironment();
        if (!graphics.isHeadlessInstance()) {
            for (GraphicsDevice device : graphics.getScreenDevices()) {
                Rectangle bounds = device.getDefaultConfiguration().getBounds();
                displays.add(
                        DisplayDetails.newBuilder()
                                .setHeight(bounds.height)
                                .setWidth(bounds.width)
                                .build());
            }
        }
        return displays;
    }

    /** Gets information about the jvm this code is running in. */
    @NonNull
    public static JvmDetails getJvmDetails() {
        RuntimeMXBean runtime = HostData.getRuntimeBean();

        JvmDetails.Builder builder =
                JvmDetails.newBuilder()
                        .setName(Strings.nullToEmpty(runtime.getVmName()))
                        .setVendor(Strings.nullToEmpty(runtime.getVmVendor()))
                        .setVersion(Strings.nullToEmpty(runtime.getVmVersion()));

        for (String vmOption : runtime.getInputArguments()) {
            parseVmOption(vmOption, builder);
        }

        return builder.build();
    }

    /** Parses known VM options into a {@link JvmDetails.Builder} */
    private static void parseVmOption(
            @NonNull String vmOption, @NonNull JvmDetails.Builder builder) {
        if (vmOption.startsWith(VM_OPTION_XMS)) {
            builder.setMinimumHeapSize(
                    parseVmOptionSize(vmOption.substring(VM_OPTION_XMS.length())));
        } else if (vmOption.startsWith(VM_OPTION_XMX)) {
            builder.setMaximumHeapSize(
                    parseVmOptionSize(vmOption.substring(VM_OPTION_XMX.length())));
        } else if (vmOption.startsWith(VM_OPTION_MAX_PERM_SIZE)) {
            builder.setMaximumPermanentSpaceSize(
                    parseVmOptionSize(vmOption.substring(VM_OPTION_MAX_PERM_SIZE.length())));
        } else if (vmOption.startsWith(VM_OPTION_RESERVED_CODE_CACHE_SIZE)) {
            builder.setMaximumCodeCacheSize(
                    parseVmOptionSize(
                            vmOption.substring(VM_OPTION_RESERVED_CODE_CACHE_SIZE.length())));
        } else if (vmOption.startsWith(VM_OPTION_SOFT_REF_LRU_POLICY_MS_PER_MB)) {
            builder.setSoftReferenceLruPolicy(
                    parseVmOptionSize(
                            vmOption.substring(VM_OPTION_SOFT_REF_LRU_POLICY_MS_PER_MB.length())));
        }

        switch (vmOption) {
            case "-XX:+UseConcMarkSweepGC":
                builder.setGarbageCollector(JvmDetails.GarbageCollector.CONCURRENT_MARK_SWEEP_GC);
                break;
            case "-XX:+UseParallelGC":
                builder.setGarbageCollector(JvmDetails.GarbageCollector.PARALLEL_GC);
                break;
            case "-XX:+UseParallelOldGC":
                builder.setGarbageCollector(JvmDetails.GarbageCollector.PARALLEL_OLD_GC);
                break;
            case "-XX:+UseSerialGC":
                builder.setGarbageCollector(JvmDetails.GarbageCollector.SERIAL_GC);
                break;
            case "-XX:+UseG1GC":
                builder.setGarbageCollector(JvmDetails.GarbageCollector.SERIAL_GC);
                break;
        }
    }

    /** Parses VM options size formatted as "[0-9]+[GgMmKk]?" into a long. */
    @VisibleForTesting
    static long parseVmOptionSize(@NonNull String vmOptionSize) {
        if (Strings.isNullOrEmpty(vmOptionSize)) {
            return EMPTY_SIZE;
        }
        try {
            for (int i = 0; i < vmOptionSize.length(); i++) {
                char c = vmOptionSize.charAt(i);
                if (!Character.isDigit(c)) {
                    if (i == 0) {
                        return NO_DIGITS;
                    }
                    String digits = vmOptionSize.substring(0, i);
                    long value = Long.parseLong(digits);
                    switch (c) {
                        case 't':
                        case 'T':
                            return value * TERABYTE;
                        case 'g':
                        case 'G':
                            return value * GIGABYTE;
                        case 'm':
                        case 'M':
                            return value * MEGABYTE;
                        case 'k':
                        case 'K':
                            return value * KILOBYTE;
                        default:
                            return INVALID_POSTFIX;
                    }
                }
            }
            return Long.parseLong(vmOptionSize);
        } catch (NumberFormatException e) {
            return INVALID_NUMBER;
        }
    }

    /** Gets stats about the current process java runtime. */
    public static JavaProcessStats getJavaProcessStats() {
        MemoryMXBean memoryBean = HostData.getMemoryBean();
        ClassLoadingMXBean classLoadingBean = HostData.getClassLoadingBean();

        return JavaProcessStats.newBuilder()
                .setHeapMemoryUsage(memoryBean.getHeapMemoryUsage().getUsed())
                .setNonHeapMemoryUsage(memoryBean.getNonHeapMemoryUsage().getUsed())
                .setLoadedClassCount(classLoadingBean.getLoadedClassCount())
                .addAllGarbageCollectionStats(getGarbageCollectionStats())
                .setThreadCount(HostData.getThreadBean().getThreadCount())
                .build();
    }

    /**
     * Gets stats about the current process' Garbage Collectors. Instead of returning cumulative
     * data since process was started, it reports stats since the last call to this method.
     */
    @VisibleForTesting
    static List<GarbageCollectionStats> getGarbageCollectionStats() {
        List<GarbageCollectionStats> stats = new ArrayList<>();
        for (GarbageCollectorMXBean gc : HostData.getGarbageCollectorBeans()) {
            String name = gc.getName();
            GarbageCollectionStatsDiffs previous = sGarbageCollectionStats.get(name);
            if (previous == null) {
                previous = new GarbageCollectionStatsDiffs();
            }
            GarbageCollectionStatsDiffs current = new GarbageCollectionStatsDiffs();
            current.collections = gc.getCollectionCount();
            long collectionsDiff = current.collections - previous.collections;

            current.time = gc.getCollectionTime();
            long timeDiff = current.time - previous.time;
            sGarbageCollectionStats.put(name, current);

            stats.add(
                    GarbageCollectionStats.newBuilder()
                            .setName(gc.getName())
                            .setGcCollections(collectionsDiff)
                            .setGcTime(timeDiff)
                            .build());
        }
        return stats;
    }
}
