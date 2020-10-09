load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_library", "kotlin_test")

# managed by go/iml_to_build [unb]
iml_module(
    name = "analytics-publisher",
    srcs = ["src/main/java"],
    iml_files = ["analytics-publisher.iml"],
    lint_baseline = "//tools/base/lint:studio-checks/empty_baseline.xml",
    project = "unb",
    test_srcs = ["src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/adt/idea/.idea/libraries:unb.Guava",
        "//tools/base/annotations:studio.android.sdktools.android-annotations",
    ],
    # do not sort: must match IML order
    deps = [
        "//prebuilts/studio/intellij-sdk:studio-sdk",
        "//tools/adt/idea/.idea/libraries:unb.Guava",
        "//tools/base/annotations:studio.android.sdktools.android-annotations[module]",
        "//tools/adt/idea/.idea/libraries:unb.truth[test]",
        "//tools/analytics-library/shared:analytics-shared[module]",
        "//tools/base/testutils:studio.android.sdktools.testutils[module, test]",
        "//tools/base/common:studio.android.sdktools.common[module]",
        "//tools/analytics-library/tracker:analytics-tracker[module, test]",
        "//tools/adt/idea/.idea/libraries:unb.protobuf",
        "//tools/adt/idea/.idea/libraries:unb.studio-analytics-proto",
        "//tools/adt/idea/.idea/libraries:unb.kotlin-stdlib-jdk8",
    ],
)

kotlin_library(
    name = "publisher",
    srcs = glob([
        "src/main/java/**/*.kt",
        "src/main/java/**/*.java",
    ]),
    pom = ":publisher.pom",
    resource_strip_prefix = "tools/analytics-library/publisher",
    visibility = ["//visibility:public"],
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/analytics-library/shared:tools.analytics-shared",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.code.gson_gson",
        "//tools/base/third_party:com.google.guava_guava",
    ],
)

load("//tools/base/bazel:maven.bzl", "maven_pom")

maven_pom(
    name = "publisher.pom",
    artifact = "publisher",
    group = "com.android.tools.analytics-library",
    source = "//tools/buildSrc/base:base_version",
)
