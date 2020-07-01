load("//tools/base/bazel:bazel.bzl", "iml_module")

# managed by go/iml_to_build
iml_module(
    name = "analytics-inspector",
    srcs = ["src/main/java"],
    iml_files = ["analytics-inspector.iml"],
    lint_baseline = "//tools/base/lint:studio-checks/empty_baseline.xml",
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
        "//tools/idea/.idea/libraries:gson",
        "//tools/idea/.idea/libraries:protobuf",
        "//tools/idea/.idea/libraries:studio-analytics-proto",
    ],
)

java_binary(
    name = "tools.analytics-inspector",
    main_class = "com.android.tools.analytics.AnalyticsInspector",
    runtime_deps = [":inspector"],
)

load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")

maven_java_library(
    name = "inspector",
    srcs = glob(["src/main/java/**"]),
    pom = ":inspector.pom",
    resource_strip_prefix = "tools/analytics-library/inspector",
    visibility = ["//visibility:public"],
    deps = [
        "//tools/analytics-library/protos/src/main/proto",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.code.gson_gson",
        "//tools/base/third_party:com.google.guava_guava",
    ],
)

maven_pom(
    name = "inspector.pom",
    artifact = "inspector",
    group = "com.android.tools.analytics-library",
    source = "//tools/buildSrc/base:base_version",
)

# managed by go/iml_to_build [unb]
iml_module(
    name = "unb.analytics-inspector",
    srcs = ["src/main/java"],
    iml_files = ["analytics-inspector.iml"],
    lint_baseline = "//tools/base/lint:studio-checks/empty_baseline.xml",
    tags = [
        "manual",
        "unb",
    ],
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
    ],
)
