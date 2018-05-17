load("//tools/base/bazel:bazel.bzl", "iml_module")

# managed by go/iml_to_build
iml_module(
    name = "analytics-publisher",
    srcs = ["src/main/java"],
    iml_files = ["analytics-publisher.iml"],
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
        "//tools/analytics-library/tracker:analytics-tracker[module, test]",
        "//tools/idea/.idea/libraries:protobuf",
        "//tools/idea/.idea/libraries:studio-analytics-proto",
    ],
)
