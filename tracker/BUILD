load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_library", "kotlin_test")

# managed by go/iml_to_build
iml_module(
    name = "analytics-tracker",
    srcs = ["src/main/java"],
    iml_files = ["analytics-tracker.iml"],
    lint_baseline = "lint_baseline.xml",
    test_srcs = ["src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/idea/.idea/libraries:Guava",
        "//tools/base/annotations:studio.android.sdktools.android-annotations",
    ],
    # do not sort: must match IML order
    deps = [
        "//tools/idea/.idea/libraries:Guava",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/base/annotations:studio.android.sdktools.android-annotations[module]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/analytics-library/shared:analytics-shared[module]",
        "//tools/base/testutils:studio.android.sdktools.testutils[module, test]",
        "//tools/base/common:studio.android.sdktools.common[module]",
        "//tools/idea/.idea/libraries:protobuf",
        "//tools/idea/.idea/libraries:studio-analytics-proto",
        "//tools/idea/.idea/libraries:kotlin-stdlib-jdk8",
    ],
)

load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")

kotlin_library(
    name = "tools.analytics-tracker",
    srcs = glob([
        "src/main/java/**/*.kt",
        "src/main/java/**/*.java",
    ]),
    pom = "analytics-tracker.pom",
    resource_strip_prefix = "tools/analytics-library/tracker",
    visibility = ["//visibility:public"],
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/analytics-library/shared:tools.analytics-shared",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.guava_guava",
    ],
)

maven_pom(
    name = "analytics-tracker.pom",
    artifact = "tracker",
    group = "com.android.tools.analytics-library",
    source = "//tools/buildSrc/base:base_version",
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

# managed by go/iml_to_build [unb]
iml_module(
    name = "unb.analytics-tracker",
    srcs = ["src/main/java"],
    iml_files = ["analytics-tracker.iml"],
    lint_baseline = "lint_baseline.xml",
    tags = [
        "manual",
        "unb",
    ],
    test_srcs = ["src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/adt/idea/.idea/libraries:unb.Guava",
        "//tools/base/annotations:unb.studio.android.sdktools.android-annotations",
    ],
    # do not sort: must match IML order
    deps = [
        "//prebuilts/studio/intellij-sdk:studio-sdk",
        "//tools/adt/idea/.idea/libraries:unb.Guava",
        "//tools/base/annotations:unb.studio.android.sdktools.android-annotations[module]",
        "//tools/adt/idea/.idea/libraries:unb.truth[test]",
        "//tools/analytics-library/shared:unb.analytics-shared[module]",
        "//tools/base/testutils:unb.studio.android.sdktools.testutils[module, test]",
        "//tools/base/common:unb.studio.android.sdktools.common[module]",
        "//tools/adt/idea/.idea/libraries:unb.protobuf",
        "//tools/adt/idea/.idea/libraries:unb.studio-analytics-proto",
        "//tools/adt/idea/.idea/libraries:unb.kotlin-stdlib-jdk8",
    ],
)
