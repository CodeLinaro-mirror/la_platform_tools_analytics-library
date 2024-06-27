load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_test")
load("//tools/base/bazel:maven.bzl", "maven_library")
load("//tools/base/common:version.bzl", "BASE_VERSION")

# managed by go/iml_to_build
iml_module(
    name = "analytics-publisher",
    srcs = ["src/main/java"],
    iml_files = ["analytics-publisher.iml"],
    lint_baseline = "lint_baseline.xml",
    test_srcs = ["src/test/java"],
    test_tags = ["noci:studio-win"],  # http://b/316139798
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = ["//tools/base/annotations:studio.android.sdktools.android-annotations"],
    # do not sort: must match IML order
    deps = [
        "@intellij//:intellij-sdk",
        "@intellij//:com.intellij.java",
        "//tools/base/annotations:studio.android.sdktools.android-annotations[module]",
        "//tools/adt/idea/.idea/libraries:truth[test]",
        "//tools/analytics-library/shared:analytics-shared[module]",
        "//tools/base/testutils:studio.android.sdktools.testutils[module, test]",
        "//tools/base/common:studio.android.sdktools.common[module]",
        "//tools/analytics-library/tracker:analytics-tracker[module, test]",
        "//tools/adt/idea/.idea/libraries:studio-analytics-proto",
    ],
)

maven_library(
    name = "publisher",
    srcs = glob([
        "src/main/java/**/*.kt",
        "src/main/java/**/*.java",
    ]),
    coordinates = "com.android.tools.analytics-library:publisher",
    description = "Library for publishing usage analytics.",
    module_name = "publisher",
    notice = "NOTICE",
    pom_name = "Android Tools Analytics Publisher",
    resource_strip_prefix = "tools/analytics-library/publisher",
    version = BASE_VERSION,
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:org.jetbrains.kotlin.kotlin-stdlib-jdk8",
    ],
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/analytics-library/shared:tools.analytics-shared",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "@maven//:com.google.guava.guava",
        "@maven//:com.google.protobuf.protobuf-java",
    ],
)
