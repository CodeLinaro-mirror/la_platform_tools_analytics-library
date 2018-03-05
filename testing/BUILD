load("//tools/base/bazel:bazel.bzl", "iml_module")

iml_module(
    name = "analytics-testing",
    iml_files = ["analytics-testing.iml"],
    tags = ["managed"],
    test_srcs = [
        "src/main/java",
        "src/test/java",
    ],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    deps = [
        "//tools/idea/.idea/libraries:Guava[test]",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/base/annotations:studio.android-annotations[module, test]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/idea/.idea/libraries:gson[test]",
        "//tools/analytics-library/protos:analytics-protos[module, test]",
        "//tools/base/testutils:studio.testutils[module, test]",
        "//tools/base/common:studio.common[module, test]",
        "//tools/analytics-library/shared:analytics-shared[module, test]",
        "//tools/analytics-library/tracker:analytics-tracker[module, test]",
    ],
)

load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")

java_library(
    name = "tools.analytics-testing",
    srcs = glob(["src/main/java/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/analytics-library/shared:tools.analytics-shared",
        "//tools/analytics-library/tracker:tools.analytics-tracker",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.guava_guava",
        "//tools/base/third_party:com.google.protobuf_protobuf-java",
    ],
)

java_test(
    name = "tools.analytics-testing_tests",
    srcs = glob(["src/test/java/**"]),
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
        "//tools/base/third_party:com.google.protobuf_protobuf-java",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:junit_junit",
    ],
)
