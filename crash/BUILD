load("//tools/base/bazel:bazel.bzl", "iml_module")
load("//tools/base/bazel:kotlin.bzl", "kotlin_test")

# managed by go/iml_to_build
iml_module(
    name = "analytics-crash",
    srcs = ["src/main/java"],
    iml_files = ["analytics-crash.iml"],
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
        "//tools/base/annotations:studio.android.sdktools.android-annotations[module]",
        "//tools/idea/.idea/libraries:commons-logging",
        "//tools/idea/.idea/libraries:http-client",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/idea/.idea/libraries:netty-codec-http[test]",
        "//tools/idea/.idea/libraries:mockito[test]",
        "//tools/idea/.idea/libraries:Objenesis[test]",
    ],
)

load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")

maven_java_library(
    name = "tools.analytics-crash",
    srcs = glob(["src/main/java/**"]),
    pom = ":analytics-crash.pom",
    resource_strip_prefix = "tools/analytics-library/crash",
    visibility = ["//visibility:public"],
    deps = [
        "//tools/base/annotations",
        "//tools/base/third_party:com.google.guava_guava",
        "//tools/base/third_party:org.apache.httpcomponents_httpmime",
    ],
)

maven_pom(
    name = "analytics-crash.pom",
    artifact = "crash",
    group = "com.android.tools.analytics-library",
    source = "//tools/buildSrc/base:base_version",
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
        "//tools/base/third_party:junit_junit",
    ],
    deps = [
        ":tools.analytics-crash",
        "//tools/base/annotations",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:io.netty_netty-all",
        "//tools/base/third_party:junit_junit",
        "//tools/base/third_party:org.apache.httpcomponents_httpmime",
        "//tools/base/third_party:org.mockito_mockito-core",
    ],
)

# managed by go/iml_to_build [unb]
iml_module(
    name = "unb.analytics-crash",
    srcs = ["src/main/java"],
    iml_files = ["analytics-crash.iml"],
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
        "//tools/adt/idea/.idea/libraries:unb.mockito[test]",
    ],
)
