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
package com.android.tools.analytics

import com.android.testutils.SystemPropertyOverrides
import com.android.tools.analytics.stubs.*
import com.google.common.collect.ImmutableList
import com.google.wireless.android.sdk.stats.*
import com.google.wireless.android.sdk.stats.DeviceInfo.ApplicationBinaryInterface
import com.google.wireless.android.sdk.stats.JvmDetails.GarbageCollector
import com.google.wireless.android.sdk.stats.ProductDetails.CpuArchitecture
import java.lang.management.MemoryUsage
import java.util.*
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

/** Tests for [CommonMetricsData]. */
class CommonMetricsDataTest {

  @Test
  fun cpuArchitectureFromStringTest() {
    assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.cpuArchitectureFromString(null))
    assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.cpuArchitectureFromString(""))
    assertEquals(CpuArchitecture.X86_64, CommonMetricsData.cpuArchitectureFromString("x86_64"))
    assertEquals(CpuArchitecture.X86_64, CommonMetricsData.cpuArchitectureFromString("ia64"))
    assertEquals(CpuArchitecture.X86_64, CommonMetricsData.cpuArchitectureFromString("amd64"))
    assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("i486"))
    assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("i586"))
    assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("i686"))
    assertEquals(CpuArchitecture.X86, CommonMetricsData.cpuArchitectureFromString("x86"))
    assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.cpuArchitectureFromString("x96"))
    assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.cpuArchitectureFromString("i6869"))
  }

  @Test
  @Throws(Exception::class)
  fun getJvmArchitectureTest() {
    SystemPropertyOverrides().use { systemPropertyOverrides ->
      systemPropertyOverrides.setProperty("os.arch", null)
      assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "")
      assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "x86_64")
      assertEquals(CpuArchitecture.X86_64, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "ia64")
      assertEquals(CpuArchitecture.X86_64, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "amd64")
      assertEquals(CpuArchitecture.X86_64, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "i486")
      assertEquals(CpuArchitecture.X86, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "i586")
      assertEquals(CpuArchitecture.X86, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "i686")
      assertEquals(CpuArchitecture.X86, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "x86")
      assertEquals(CpuArchitecture.X86, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "x96")
      assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.jvmArchitecture)
      systemPropertyOverrides.setProperty("os.arch", "i6869")
      assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.jvmArchitecture)
    }
  }

  @Test
  @Throws(Exception::class)
  fun getOsArchitectureTest() {
    // Override system properties for 'os.arch' and 'os.name'.
    try {
      SystemPropertyOverrides().use { systemPropertyOverrides ->
        systemPropertyOverrides.setProperty("os.arch", null)
        assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.jvmArchitecture)
        systemPropertyOverrides.setProperty("os.arch", "")
        assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.jvmArchitecture)
        systemPropertyOverrides.setProperty("os.arch", "x86_64")
        assertEquals(CpuArchitecture.X86_64, CommonMetricsData.jvmArchitecture)
        systemPropertyOverrides.setProperty("os.arch", "i6869")
        assertEquals(CpuArchitecture.UNKNOWN_CPU_ARCHITECTURE, CommonMetricsData.jvmArchitecture)

        systemPropertyOverrides.setProperty("os.arch", "x86")
        systemPropertyOverrides.setProperty("os.name", "Windows 10")
        EnvironmentFakes.setSingleProperty("PROCESSOR_ARCHITEW6432", "AMD64")
        assertEquals(CpuArchitecture.X86_64, CommonMetricsData.osArchitecture)
        systemPropertyOverrides.setProperty("os.name", "Linux")
        EnvironmentFakes.setSingleProperty("HOSTTYPE", "x86_64")
        assertEquals(CpuArchitecture.X86_64, CommonMetricsData.osArchitecture)
      }
    } finally {
      Environment.instance = Environment.SYSTEM
    }
  }

  @Test
  @Throws(Exception::class)
  fun getOsNameTest() {
    // Override system properties for 'os.name'.
    SystemPropertyOverrides().use { systemPropertyOverrides ->
      // Test no os specified.
      systemPropertyOverrides.setProperty("os.name", "")
      assertEquals("unknown", CommonMetricsData.osName)
      // Test our supported OSes.
      systemPropertyOverrides.setProperty("os.name", "Linux")
      assertEquals("linux", CommonMetricsData.osName)
      systemPropertyOverrides.setProperty("os.name", "Windows 10")
      assertEquals("windows", CommonMetricsData.osName)
      systemPropertyOverrides.setProperty("os.name", "Windows Vista")
      assertEquals("windows", CommonMetricsData.osName)
      systemPropertyOverrides.setProperty("os.name", "Mac OS X")
      assertEquals("macosx", CommonMetricsData.osName)
      // Test unknown Oses.
      systemPropertyOverrides.setProperty("os.name", "My Custom OS")
      assertEquals("My Custom OS", CommonMetricsData.osName)
      val customLong = "My Custom OS With a really realy long name"
      systemPropertyOverrides.setProperty("os.name", customLong)
      assertEquals(customLong.substring(0, 32), CommonMetricsData.osName)
    }
  }

  @Test
  @Throws(Exception::class)
  fun getMajorOsVersionTest() {
    // Override system properties for 'os.version'.
    SystemPropertyOverrides().use { systemPropertyOverrides ->
      // Test no version specified.
      systemPropertyOverrides.setProperty("os.version", "3")
      assertEquals(null, CommonMetricsData.majorOsVersion)
      // Test supported os version numbers.
      systemPropertyOverrides.setProperty("os.version", "3.13.0-85-generic")
      assertEquals("3.13", CommonMetricsData.majorOsVersion)
      systemPropertyOverrides.setProperty("os.version", "10.7.4")
      assertEquals("10.7", CommonMetricsData.majorOsVersion)
      systemPropertyOverrides.setProperty("os.version", "10.0")
      assertEquals("10.0", CommonMetricsData.majorOsVersion)
      // Test unsupported os version numbers.
      systemPropertyOverrides.setProperty("os.version", "a.b.c")
      assertEquals(null, CommonMetricsData.majorOsVersion)
    }
  }

  @Test
  fun applicationBinaryInterfaceFromStringTest() {
    assertEquals(ApplicationBinaryInterface.ARME_ABI, CommonMetricsData.applicationBinaryInterfaceFromString("armeabi"))
    assertEquals(ApplicationBinaryInterface.ARME_ABI_V6J, CommonMetricsData.applicationBinaryInterfaceFromString("armeabi-v6j"))
    assertEquals(ApplicationBinaryInterface.ARME_ABI_V6L, CommonMetricsData.applicationBinaryInterfaceFromString("armeabi-v6l"))
    assertEquals(ApplicationBinaryInterface.ARME_ABI_V7A, CommonMetricsData.applicationBinaryInterfaceFromString("armeabi-v7a"))
    assertEquals(ApplicationBinaryInterface.ARM64_V8A_ABI, CommonMetricsData.applicationBinaryInterfaceFromString("arm64-v8a"))
    assertEquals(ApplicationBinaryInterface.MIPS_ABI, CommonMetricsData.applicationBinaryInterfaceFromString("mips"))
    assertEquals(ApplicationBinaryInterface.MIPS_R2_ABI, CommonMetricsData.applicationBinaryInterfaceFromString("mips-r2"))
    assertEquals(ApplicationBinaryInterface.X86_ABI, CommonMetricsData.applicationBinaryInterfaceFromString("x86"))
    assertEquals(ApplicationBinaryInterface.X86_64_ABI, CommonMetricsData.applicationBinaryInterfaceFromString("x86_64"))
    assertEquals(ApplicationBinaryInterface.UNKNOWN_ABI, CommonMetricsData.applicationBinaryInterfaceFromString(null))
    assertEquals(ApplicationBinaryInterface.UNKNOWN_ABI, CommonMetricsData.applicationBinaryInterfaceFromString(""))
    assertEquals(ApplicationBinaryInterface.UNKNOWN_ABI, CommonMetricsData.applicationBinaryInterfaceFromString("my_custom_abi"))
  }

  @Test
  fun parseVmOptionSizeTest() {
    // Test various valid values for parsing vm option size in form of:
    // "[0-9]+[GgMmKk]?" to be valid as well as various invalid values.
    assertEquals(CommonMetricsData.EMPTY_SIZE.toLong(), CommonMetricsData.parseVmOptionSize(""))
    assertEquals(1L, CommonMetricsData.parseVmOptionSize("1"))
    assertEquals(1024L, CommonMetricsData.parseVmOptionSize("1024"))
    assertEquals(1024L, CommonMetricsData.parseVmOptionSize("1k"))
    assertEquals(20480L, CommonMetricsData.parseVmOptionSize("20k"))
    assertEquals(2L * 1024 * 1024, CommonMetricsData.parseVmOptionSize("2M"))
    assertEquals(10L * 1024 * 1024 * 1024, CommonMetricsData.parseVmOptionSize("10G"))
    assertEquals(2L * 1024 * 1024 * 1024 * 1024, CommonMetricsData.parseVmOptionSize("2T"))
    assertEquals(CommonMetricsData.NO_DIGITS.toLong(), CommonMetricsData.parseVmOptionSize("G"))
    assertEquals(CommonMetricsData.INVALID_POSTFIX.toLong(), CommonMetricsData.parseVmOptionSize("10Z"))
    assertEquals(
      CommonMetricsData.INVALID_NUMBER.toLong(),
      CommonMetricsData.parseVmOptionSize(java.lang.Long.toString(java.lang.Long.MAX_VALUE) + 0),
    )
  }

  @Test
  fun getJvmDetailsTest() {
    val vmOptions = ArrayList<String>()

    // Stub out the Runtime MX Bean to get consistent naming in test.
    HostData.runtimeBean =
      object : StubRuntimeMXBean() {
        override fun getVmName(): String? {
          return VM_NAME
        }

        override fun getVmVendor(): String? {
          return VM_VENDOR
        }

        override fun getVmVersion(): String? {
          return VM_VERSION
        }

        override fun getInputArguments(): List<String>? {
          return vmOptions
        }
      }

    try {
      // Test getJvmDetails w/o any VM options specified.
      val expectedNoOptions = JvmDetails.newBuilder().setName(VM_NAME).setVendor(VM_VENDOR).setVersion(VM_VERSION).build()
      val resultNoOptions = CommonMetricsData.jvmDetails
      assertEquals(expectedNoOptions, resultNoOptions)

      // Test getJvmDetails with the default studio VM options specified.
      vmOptions.add("-server")
      vmOptions.add("-Xms256m")
      vmOptions.add("-Xmx750m")
      vmOptions.add("-XX:MaxPermSize=350m")
      vmOptions.add("-XX:ReservedCodeCacheSize=240m")
      vmOptions.add("-XX:+UseConcMarkSweepGC")
      vmOptions.add("-XX:SoftRefLRUPolicyMSPerMB=50")
      vmOptions.add("-ea")
      vmOptions.add("-XX:-OmitStackTraceInFastThrow")
      vmOptions.add("-Djna.nosys=true")
      vmOptions.add("-Djna.boot.library.path=")
      vmOptions.add("-Djna.debug_load=true")
      vmOptions.add("-Djna.debug_load.jna=true")
      vmOptions.add("-Dsun.io.useCanonCaches=false")
      vmOptions.add("-Djava.net.preferIPv4Stack=true")
      vmOptions.add("-XX:+HeapDumpOnOutOfMemoryError")
      vmOptions.add("-XX:-OmitStackTraceInFastThrow")
      vmOptions.add("-Dawt.useSystemAAFontSettings=lcd")

      val expectedAllOptions =
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
          .build()
      val resultAllOptions = CommonMetricsData.jvmDetails
      assertEquals(expectedAllOptions, resultAllOptions)
    } finally {
      // undo the stubbing of Runtime MX Bean.
      HostData.runtimeBean = null
    }
  }

  @Test
  fun getGarbageCollectionStatsTest() {
    try {
      // Stub out the Garbage Collector MX Bean for consistent GC stats in this test.
      HostData.garbageCollectorBeans =
        ImmutableList.of(
          StubGarbageCollectionBean.fixedValue(FIRST_GC, 100, 123),
          StubGarbageCollectionBean.fixedValue(SECOND_GC, 404, 512),
        )

      val firstExpected = GarbageCollectionStats.newBuilder().setName(FIRST_GC).setGcCollections(100).setGcTime(123).build()

      val secondExpected = GarbageCollectionStats.newBuilder().setName(SECOND_GC).setGcCollections(404).setGcTime(512).build()

      val results1 = CommonMetricsData.garbageCollectionStats
      Assert.assertEquals(2, results1.size.toLong())

      assertEquals(firstExpected, results1[0])
      assertEquals(secondExpected, results1[1])

      // Update the Garbage Collector MX Beans Stub with new values.
      HostData.garbageCollectorBeans =
        ImmutableList.of(
          StubGarbageCollectionBean.fixedValue(FIRST_GC, 200, 234),
          StubGarbageCollectionBean.fixedValue(SECOND_GC, 501, 1024),
        )

      // We expect results to be a diff instead of commulative of above values.
      val thirdExpected = GarbageCollectionStats.newBuilder().setName(FIRST_GC).setGcCollections(100).setGcTime(111).build()

      val fourthExpected = GarbageCollectionStats.newBuilder().setName(SECOND_GC).setGcCollections(97).setGcTime(512).build()

      val results2 = CommonMetricsData.garbageCollectionStats
      Assert.assertEquals(2, results2.size.toLong())

      assertEquals(thirdExpected, results2[0])
      assertEquals(fourthExpected, results2[1])
    } finally {
      // undo the stubbing of Garbage Collector MX Beans.
      HostData.garbageCollectorBeans = null
      // Reset our collection stats for future tests.
      CommonMetricsData.garbageCollectionStatsCache.clear()
    }
  }

  @Test
  fun getJavaProcessStatsTest() {
    try {
      // Stub out the Garbage Collector MX Bean for consistent GC stats in this test.
      HostData.garbageCollectorBeans =
        ImmutableList.of(
          StubGarbageCollectionBean.fixedValue(FIRST_GC, 100, 123),
          StubGarbageCollectionBean.fixedValue(SECOND_GC, 404, 512),
        )

      // Stub out the Memory MX Bean for consistent memory stats in this test.
      HostData.memoryBean =
        object : StubMemoryBean() {
          override fun getHeapMemoryUsage(): MemoryUsage? {
            return MemoryUsage(1, 2, 3, 4)
          }

          override fun getNonHeapMemoryUsage(): MemoryUsage? {
            return MemoryUsage(5, 6, 7, 8)
          }
        }

      // Stub out the Class Loading MX Bean for consistent class stats in this test.
      HostData.classLoadingBean =
        object : StubClassLoadingBean() {
          override fun getLoadedClassCount(): Int {
            return 100
          }
        }

      HostData.threadBean =
        object : StubThreadBean() {
          override fun getThreadCount(): Int {
            return 5
          }
        }

      val expected =
        JavaProcessStats.newBuilder()
          .setHeapMemoryUsage(2)
          .setNonHeapMemoryUsage(6)
          .setLoadedClassCount(100)
          .addGarbageCollectionStats(GarbageCollectionStats.newBuilder().setName(FIRST_GC).setGcCollections(100).setGcTime(123).build())
          .addGarbageCollectionStats(GarbageCollectionStats.newBuilder().setName(SECOND_GC).setGcCollections(404).setGcTime(512).build())
          .setThreadCount(5)
          .build()

      val result = CommonMetricsData.javaProcessStats
      Assert.assertEquals(expected, result)
    } finally {
      // undo the stubbing of the various MX Beans.
      HostData.garbageCollectorBeans = null
      HostData.memoryBean = null
      HostData.classLoadingBean = null
      // Reset our collection stats for future tests.
      CommonMetricsData.garbageCollectionStatsCache.clear()
    }
  }

  companion object {
    private const val VM_NAME = "VM Name"
    private const val VM_VENDOR = "VM Vendor"
    private const val VM_VERSION = "VM Version"
    private const val FIRST_GC = "FirstGC"
    private const val SECOND_GC = "SecondGC"
  }
}
