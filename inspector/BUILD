load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "maven_library")
load("//tools/base/common:version.bzl", "BASE_VERSION")

# managed by go/iml_to_build
iml_module(
    name = "analytics-inspector",
    srcs = ["src/main/java"],
    iml_files = ["analytics-inspector.iml"],
    lint_baseline = "lint_baseline.xml",
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

java_binary(
    name = "tools.analytics-inspector",
    main_class = "com.android.tools.analytics.AnalyticsInspector",
    runtime_deps = [":inspector"],
)

maven_library(
    name = "inspector_bzl",
    srcs = glob(["src/main/java/**"]),
    coordinates = "com.android.tools.analytics-library:inspector:" + BASE_VERSION,
    legacy_name = "inspector",
    notice = "NOTICE",
    resource_strip_prefix = "tools/analytics-library/inspector",
    visibility = ["//visibility:public"],
    deps = [
        "//tools/analytics-library/protos/src/main/proto:proto_bzl",
        "//tools/base/annotations:annotations_bzl",
        "//tools/base/common:common_bzl",
        "@maven//:com.google.code.gson.gson",
        "@maven//:com.google.guava.guava",
    ],
)
