load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_library", "kotlin_test")
load("//tools/base/bazel:maven.bzl", "maven_library")
load("//tools/base/common:version.bzl", "BASE_VERSION")

# managed by go/iml_to_build
iml_module(
    name = "analytics-shared",
    srcs = ["src/main/java"],
    iml_files = ["analytics-shared.iml"],
    jvm_target = "8",
    # do not sort: must match IML order
    test_deps = [
        "//tools/adt/idea/.idea/libraries:junit4",
        "//tools/adt/idea/.idea/libraries:truth",
        "//tools/base/testutils:studio.android.sdktools.testutils",
    ],
    test_srcs = ["src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = ["//tools/base/annotations:studio.android.sdktools.android-annotations"],
    # do not sort: must match IML order
    deps = [
        "@intellij//:intellij-sdk",
        "@intellij//:com.intellij.java",
        "//tools/base/annotations:studio.android.sdktools.android-annotations",
        "//tools/base/common:studio.android.sdktools.common",
        "//tools/adt/idea/.idea/libraries:studio-analytics-proto",
    ],
)

maven_library(
    name = "tools.analytics-shared",
    srcs = glob([
        "src/main/java/**/*.kt",
        "src/main/java/**/*.java",
    ]),
    coordinates = "com.android.tools.analytics-library:shared",
    description = "Helper Library for tracking usage analytics.",
    module_name = "shared",
    notice = "NOTICE",
    pom_name = "Android Tools Analytics Shared Library",
    resource_strip_prefix = "tools/analytics-library/shared",
    version = BASE_VERSION,
    visibility = ["//visibility:public"],
    exports = [
        "//tools/analytics-library/protos/src/main/proto",
    ],
    deps = [
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "@maven//:com.google.code.gson.gson",
        "@maven//:com.google.guava.guava",
        "@maven//:net.java.dev.jna.jna-platform",
        "@maven//:org.jetbrains.kotlin.kotlin-stdlib-jdk8",
    ],
)

kotlin_test(
    name = "tools.analytics-shared_tests",
    srcs = glob([
        "src/test/java/**/*.kt",
        "src/test/java/**/*.java",
    ]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-shared_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":tools.analytics-shared",
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "@maven//:com.google.code.gson.gson",
        "@maven//:com.google.truth.truth",
        "@maven//:junit.junit",
        "@maven//:org.jetbrains.kotlin.kotlin-stdlib",
        "@maven//:org.jetbrains.kotlin.kotlin-stdlib-jdk7",
        "@maven//:org.jetbrains.kotlin.kotlin-stdlib-jdk8",
    ],
)
