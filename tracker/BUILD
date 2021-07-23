load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_test", "maven_library")
load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")
load("//tools/base/common:version.bzl", "BASE_VERSION")

# managed by go/iml_to_build
iml_module(
    name = "analytics-tracker",
    srcs = ["src/main/java"],
    iml_files = ["analytics-tracker.iml"],
    lint_baseline = "lint_baseline.xml",
    test_srcs = ["src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = ["//tools/base/annotations:studio.android.sdktools.android-annotations"],
    # do not sort: must match IML order
    deps = [
        "//prebuilts/studio/intellij-sdk:studio-sdk",
        "//tools/base/annotations:studio.android.sdktools.android-annotations[module]",
        "//tools/adt/idea/.idea/libraries:truth[test]",
        "//tools/analytics-library/shared:analytics-shared[module]",
        "//tools/base/testutils:studio.android.sdktools.testutils[module, test]",
        "//tools/base/common:studio.android.sdktools.common[module]",
        "//tools/adt/idea/.idea/libraries:studio-analytics-proto",
    ],
)

maven_library(
    name = "tools.analytics-tracker_bzl",
    srcs = glob([
        "src/main/java/**/*.kt",
        "src/main/java/**/*.java",
    ]),
    coordinates = "com.android.tools.analytics-library:tracker:" + BASE_VERSION,
    legacy_name = "tools.analytics-tracker",
    notice = "NOTICE",
    resource_strip_prefix = "tools/analytics-library/tracker",
    visibility = ["//visibility:public"],
    deps = [
        "//tools/analytics-library/protos/src/main/proto:proto_bzl",
        "//tools/analytics-library/shared:tools.analytics-shared_bzl",
        "//tools/base/annotations:annotations_bzl",
        "//tools/base/common:common_bzl",
        "@maven//:com.google.guava.guava",
    ],
)

kotlin_test(
    name = "tools.analytics-tracker_tests",
    srcs = glob([
        "src/test/java/**/*.kt",
        "src/test/java/**/*.java",
    ]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-tracker_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/analytics-library/shared:tools.analytics-shared",
        "//tools/analytics-library/tracker:tools.analytics-tracker",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.protobuf_protobuf-java",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:junit_junit",
    ],
)

filegroup(
    name = "agp_gradle_build_files",
    srcs = glob([
        "src/main/**/*.java",
        "src/main/**/*.kt",
        "src/main/resources/**",
    ]) + ["build.gradle"],
    visibility = ["//tools/base/build-system:__pkg__"],
)
