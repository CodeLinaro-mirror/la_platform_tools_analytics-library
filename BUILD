# This file has been automatically generated, please do not modify directly.
load("//tools/base/bazel:bazel.bzl", "iml_module")

iml_module(
    name = "analytics-protos",
    srcs = ["protos/src/main/java"],
    deps = ["//tools:idea/lib/protobuf-2.5.0"],
    exports = ["//tools:idea/lib/protobuf-2.5.0"],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)

iml_module(
    name = "analytics-shared",
    srcs = ["shared/src/main/java"],
    test_srcs = ["shared/src/test/java"],
    deps = [
        "//tools:idea/lib/guava-18.0",
        "//tools:idea/lib/hamcrest-core-1.3[test]",
        "//tools:idea/lib/junit-4.12[test]",
        "//tools/base/annotations:android-annotations[module]",
        "//prebuilts/tools/common/m2/repository/com/google/truth/truth/0.28:jar[test]",
        "//tools:idea/lib/gson-2.5",
        "//tools/analytics-library:analytics-protos[module]",
        "//tools/base/testutils:testutils[module, test]",
        "//tools/base/common:common[module]",
    ],
    exports = [
        "//tools:idea/lib/guava-18.0",
        "//tools/base/annotations:android-annotations",
    ],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)

iml_module(
    name = "analytics-tracker",
    srcs = ["tracker/src/main/java"],
    test_srcs = ["tracker/src/test/java"],
    deps = [
        "//tools:idea/lib/guava-18.0",
        "//tools:idea/lib/hamcrest-core-1.3[test]",
        "//tools:idea/lib/junit-4.12[test]",
        "//tools/base/annotations:android-annotations[module]",
        "//prebuilts/tools/common/m2/repository/com/google/truth/truth/0.28:jar[test]",
        "//tools/analytics-library:analytics-protos[module]",
        "//tools/analytics-library:analytics-shared[module]",
        "//tools/base/testutils:testutils[module, test]",
        "//tools/base/common:common[module]",
    ],
    exports = [
        "//tools:idea/lib/guava-18.0",
        "//tools/base/annotations:android-annotations",
    ],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)

iml_module(
    name = "analytics-publisher",
    srcs = ["publisher/src/main/java"],
    test_srcs = ["publisher/src/test/java"],
    deps = [
        "//tools:idea/lib/guava-18.0",
        "//tools:idea/lib/hamcrest-core-1.3[test]",
        "//tools:idea/lib/junit-4.12[test]",
        "//tools/base/annotations:android-annotations[module]",
        "//prebuilts/tools/common/m2/repository/com/google/truth/truth/0.28:jar[test]",
        "//tools/analytics-library:analytics-protos[module]",
        "//tools/analytics-library:analytics-shared[module]",
        "//tools/base/testutils:testutils[module, test]",
        "//tools/base/common:common[module]",
        "//tools/analytics-library:analytics-tracker[module, test]",
    ],
    exports = [
        "//tools:idea/lib/guava-18.0",
        "//tools/base/annotations:android-annotations",
    ],
    javacopts = ["-extra_checks:off"],
    visibility = ["//visibility:public"],
)
