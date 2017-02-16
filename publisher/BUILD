load("//tools/base/bazel:bazel.bzl", "iml_module")

iml_module(
    name = "analytics-publisher",
    srcs = ["src/main/java"],
    tags = ["managed"],
    test_srcs = ["src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/idea/.idea/libraries:Guava",
        "//tools/base/annotations:studio.android-annotations",
    ],
    # do not sort: must match IML order
    deps = [
        "//tools/idea/.idea/libraries:Guava",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/base/annotations:studio.android-annotations[module]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/analytics-library/protos:analytics-protos[module]",
        "//tools/analytics-library/shared:analytics-shared[module]",
        "//tools/base/testutils:studio.testutils[module, test]",
        "//tools/base/common:studio.common[module]",
        "//tools/analytics-library/tracker:analytics-tracker[module, test]",
    ],
)
