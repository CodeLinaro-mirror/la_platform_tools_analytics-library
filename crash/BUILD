load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_test")
load("//tools/base/bazel:maven.bzl", "maven_library")
load("//tools/base/common:version.bzl", "BASE_VERSION")

# managed by go/iml_to_build
iml_module(
    name = "analytics-crash",
    srcs = ["src/main/java"],
    iml_files = ["analytics-crash.iml"],
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
        "//tools/adt/idea/.idea/libraries:mockito[test]",
    ],
)

maven_library(
    name = "tools.analytics-crash",
    srcs = glob(["src/main/java/**"]),
    coordinates = "com.android.tools.analytics-library:crash:" + BASE_VERSION,
    notice = "NOTICE",
    resource_strip_prefix = "tools/analytics-library/crash",
    visibility = ["//visibility:public"],
    deps = [
        "//tools/base/annotations",
        "@maven//:com.google.guava.guava",
        "@maven//:org.apache.httpcomponents.httpclient",
        "@maven//:org.apache.httpcomponents.httpcore",
        "@maven//:org.apache.httpcomponents.httpmime",
    ],
)

kotlin_test(
    name = "tools.analytics-crash_tests",
    srcs = glob([
        "src/test/java/**/*.kt",
        "src/test/java/**/*.java",
    ]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-crash_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    runtime_deps = [
        # Need to put this on the classpath before TestRunner_deploy.jar which contains
        # old JUnit classes. See https://github.com/bazelbuild/bazel/issues/2146.
        "@maven//:junit.junit",
    ],
    deps = [
        ":tools.analytics-crash",
        "//tools/base/annotations",
        "//tools/base/testutils:tools.testutils",
        "@maven//:com.google.truth.truth",
        "@maven//:io.netty.netty-all",
        "@maven//:junit.junit",
        "@maven//:org.apache.httpcomponents.httpmime",
        "@maven//:org.mockito.mockito-core",
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
