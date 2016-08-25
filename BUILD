# This file has been automatically generated, please do not modify directly.
load("//tools/base/bazel:bazel.bzl", "iml_module")

iml_module(
    name = "analytics-protos",
    srcs = ["protos/src/main/java"],
    deps = ["//tools/idea/.idea/libraries:protobuf"],
    exports = ["//tools/idea/.idea/libraries:protobuf"],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)

iml_module(
    name = "analytics-shared",
    srcs = ["shared/src/main/java"],
    test_srcs = ["shared/src/test/java"],
    deps = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/base/annotations:studio.android-annotations[module]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/idea/.idea/libraries:gson",
        "//tools/analytics-library:analytics-protos[module]",
        "//tools/base/testutils:studio.testutils[module, test]",
        "//tools/base/common:studio.common[module]",
    ],
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)

iml_module(
    name = "analytics-tracker",
    srcs = ["tracker/src/main/java"],
    test_srcs = ["tracker/src/test/java"],
    deps = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/base/annotations:studio.android-annotations[module]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/analytics-library:analytics-protos[module]",
        "//tools/analytics-library:analytics-shared[module]",
        "//tools/base/testutils:studio.testutils[module, test]",
        "//tools/base/common:studio.common[module]",
    ],
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)

iml_module(
    name = "analytics-publisher",
    srcs = ["publisher/src/main/java"],
    test_srcs = ["publisher/src/test/java"],
    deps = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/base/annotations:studio.android-annotations[module]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/analytics-library:analytics-protos[module]",
        "//tools/analytics-library:analytics-shared[module]",
        "//tools/base/testutils:studio.testutils[module, test]",
        "//tools/base/common:studio.common[module]",
        "//tools/analytics-library:analytics-tracker[module, test]",
    ],
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)
