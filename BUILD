load("//tools/base/bazel:bazel.bzl", "iml_module")

iml_module(
    name = "analytics-protos",
    srcs = ["protos/src/main/java"],
    javacopts = ["-extra_checks:off"],
    tags = ["managed"],
    visibility = ["//visibility:public"],
    exports = ["//tools/idea/.idea/libraries:protobuf"],
    deps = ["//tools/idea/.idea/libraries:protobuf"],
)

iml_module(
    name = "analytics-shared",
    srcs = ["shared/src/main/java"],
    javacopts = ["-extra_checks:off"],
    tags = ["managed"],
    test_srcs = ["shared/src/test/java"],
    visibility = ["//visibility:public"],
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
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
)

iml_module(
    name = "analytics-tracker",
    srcs = ["tracker/src/main/java"],
    javacopts = ["-extra_checks:off"],
    tags = ["managed"],
    test_srcs = ["tracker/src/test/java"],
    visibility = ["//visibility:public"],
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
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
)

iml_module(
    name = "analytics-publisher",
    srcs = ["publisher/src/main/java"],
    javacopts = ["-extra_checks:off"],
    tags = ["managed"],
    test_srcs = ["publisher/src/test/java"],
    visibility = ["//visibility:public"],
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
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
)

# TODO: Change iml_module generator to prepend "studio." to names above.
# TODO: Split this BUILD file into separate BUILD files in subdirectories.
java_library(
    name = "tools.analytics-shared",
    srcs = glob(["shared/src/main/java/**"]),
    visibility = ["//visibility:public"],
    deps = [
        ":analytics-protos",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.code.gson_gson",
        "//tools/base/third_party:com.google.guava_guava",
    ],
)

java_test(
    name = "tools.analytics-shared_tests",
    srcs = glob(["shared/src/test/java/**"]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-shared_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":analytics-protos",
        ":tools.analytics-shared",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.code.gson_gson",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:junit_junit",
    ],
)

java_library(
    name = "tools.analytics-tracker",
    srcs = glob(["tracker/src/main/java/**"]),
    visibility = ["//visibility:public"],
    deps = [
        ":analytics-protos",
        ":tools.analytics-shared",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.guava_guava",
    ],
)

java_test(
    name = "tools.analytics-tracker_tests",
    srcs = glob(["tracker/src/test/java/**"]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-tracker_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":analytics-protos",
        ":tools.analytics-shared",
        ":tools.analytics-tracker",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:junit_junit",
    ],
)
