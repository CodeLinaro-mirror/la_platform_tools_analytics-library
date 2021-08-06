load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_library", "kotlin_test")

# managed by go/iml_to_build
iml_module(
    name = "android.sdktools.analytics-testing",
    iml_files = ["android.sdktools.analytics-testing.iml"],
    test_srcs = [
        "src/main/java",
        "src/test/java",
    ],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    deps = [
        "//prebuilts/studio/intellij-sdk:studio-sdk",
        "//tools/base/annotations:studio.android.sdktools.android-annotations[module, test]",
        "//tools/adt/idea/.idea/libraries:truth[test]",
        "//tools/base/testutils:studio.android.sdktools.testutils[module, test]",
        "//tools/base/common:studio.android.sdktools.common[module, test]",
        "//tools/analytics-library/shared:analytics-shared[module, test]",
        "//tools/analytics-library/tracker:analytics-tracker[module, test]",
        "//tools/adt/idea/.idea/libraries:studio-analytics-proto",
    ],
)

kotlin_library(
    name = "tools.analytics-testing",
    srcs = glob([
        "src/main/java/**/*.kt",
        "src/main/java/**/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/analytics-library/shared:tools.analytics-shared",
        "//tools/analytics-library/tracker:tools.analytics-tracker",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "@maven//:com.google.guava.guava",
        "@maven//:com.google.protobuf.protobuf-java",
    ],
)

kotlin_test(
    name = "tools.analytics-testing_tests",
    srcs = glob([
        "src/test/java/**/*.kt",
        "src/test/java/**/*.java",
    ]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-testing_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/analytics-library/shared:tools.analytics-shared",
        "//tools/analytics-library/testing:tools.analytics-testing",
        "//tools/analytics-library/tracker:tools.analytics-tracker",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "@maven//:com.google.protobuf.protobuf-java",
        "@maven//:com.google.truth.truth",
        "@maven//:junit.junit",
    ],
)

filegroup(
    name = "agp_gradle_build_files",
    srcs = glob([
        "src/main/**/*.java",
        "src/main/**/*.kt",
        "src/main/resources/**",
    ]) + [
        "build.gradle",
        "NOTICE",
    ],
    visibility = ["//tools/base/build-system:__pkg__"],
)
