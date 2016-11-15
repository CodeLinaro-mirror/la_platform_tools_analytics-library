load("//tools/base/bazel:bazel.bzl", "iml_module")

iml_module(
    name = "analytics-protos",
    srcs = ["protos/src/main/java"],
    tags = ["managed"],
    visibility = ["//visibility:public"],
    exports = ["//tools/idea/.idea/libraries:protobuf"],
    deps = ["//tools/idea/.idea/libraries:protobuf"],
)

iml_module(
    name = "analytics-shared",
    srcs = ["shared/src/main/java"],
    tags = ["managed"],
    test_srcs = ["shared/src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
    # do not sort: must match IML order
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
    tags = ["managed"],
    test_srcs = ["tracker/src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
    # do not sort: must match IML order
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
    tags = ["managed"],
    test_srcs = ["publisher/src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
    # do not sort: must match IML order
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

iml_module(
    name = "analytics-testing",
    srcs = ["testing/src/main/java"],
    tags = ["managed"],
    test_srcs = ["testing/src/test/java"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    exports = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/base/annotations:studio.android-annotations",
    ],
    # do not sort: must match IML order
    deps = [
        "//tools/idea/.idea/libraries:guava-tools",
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/base/annotations:studio.android-annotations[module]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/idea/.idea/libraries:gson",
        "//tools/analytics-library:analytics-protos[module]",
        "//tools/base/testutils:studio.testutils[module]",
        "//tools/base/common:studio.common[module]",
        "//tools/analytics-library:analytics-shared[module]",
        "//tools/analytics-library:analytics-tracker[module]",
    ],
)

# TODO: Change iml_module generator to prepend "studio." to names above.
# TODO: Split this BUILD file into separate BUILD files in subdirectories.

load("//tools/base/bazel:proto.bzl", "java_proto_library")
load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")

java_proto_library(
    name = "tools.analytics-protos",
    pom = ":analytics-protos.pom",
    srcs = glob(["protos/src/main/proto/*.proto"]),
    visibility = ["//visibility:public"],
)

maven_pom(
  name = "analytics-protos.pom",
  artifact = "protos",
  group = "com.android.tools.analytics-library",
  source = "//tools/buildSrc/base:base_version",
)

maven_java_library(
    name = "tools.analytics-shared",
    pom = ":analytics-shared.pom",
    srcs = glob(["shared/src/main/java/**"]),
    visibility = ["//visibility:public"],
    deps = [
        ":tools.analytics-protos",
        "//tools/base/annotations:annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.code.gson_gson",
        "//tools/base/third_party:com.google.guava_guava",
    ],
)

maven_pom(
    name = "analytics-shared.pom",
    artifact = "shared",
    group = "com.android.tools.analytics-library",
    source = "//tools/buildSrc/base:base_version",
)

java_test(
    name = "tools.analytics-shared_tests",
    srcs = glob(["shared/src/test/java/**"]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-shared_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":tools.analytics-protos",
        ":tools.analytics-shared",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.code.gson_gson",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:junit_junit",
    ],
)

maven_java_library(
    name = "tools.analytics-tracker",
    pom = "analytics-tracker.pom",
    srcs = glob(["tracker/src/main/java/**"]),
    visibility = ["//visibility:public"],
    deps = [
        ":tools.analytics-protos",
        ":tools.analytics-shared",
        "//tools/base/annotations:annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.guava_guava",
    ],
)

maven_pom(
    name = "analytics-tracker.pom",
    artifact = "tracker",
    group = "com.android.tools.analytics-library",
    source = "//tools/buildSrc/base:base_version",
)

java_test(
    name = "tools.analytics-tracker_tests",
    srcs = glob(["tracker/src/test/java/**"]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-tracker_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":tools.analytics-protos",
        ":tools.analytics-shared",
        ":tools.analytics-tracker",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.protobuf_protobuf-java",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:junit_junit",
    ],
)


java_library(
    name = "tools.analytics-testing",
    srcs = glob(["testing/src/main/java/**"]),
    visibility = ["//visibility:public"],
    deps = [
        ":tools.analytics-protos",
        ":tools.analytics-shared",
        ":tools.analytics-tracker",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.guava_guava",
        "//tools/base/third_party:com.google.protobuf_protobuf-java",
    ],
)

java_test(
    name = "tools.analytics-testing_tests",
    srcs = glob(["testing/src/test/java/**"]),
    jvm_flags = ["-Dtest.suite.jar=tools.analytics-testing_tests.jar"],
    test_class = "com.android.testutils.JarTestSuite",
    deps = [
        ":tools.analytics-protos",
        ":tools.analytics-shared",
        ":tools.analytics-tracker",
        ":tools.analytics-testing",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/testutils:tools.testutils",
        "//tools/base/third_party:com.google.protobuf_protobuf-java",
        "//tools/base/third_party:com.google.truth_truth",
        "//tools/base/third_party:junit_junit",
    ],
)

java_binary(
    name = "tools.analytics-inspector",
    srcs = glob(["inspector/src/main/java/**"]),
    main_class = "com.android.tools.analytics.AnalyticsInspector",
    visibility = ["//visibility:public"],
    deps = [
        ":tools.analytics-protos",
        ":tools.analytics-shared",
        "//tools/base/annotations",
        "//tools/base/common:tools.common",
        "//tools/base/third_party:com.google.code.gson_gson",
        "//tools/base/third_party:com.google.guava_guava",
        "//tools/base/third_party:com.google.protobuf_protobuf-java",
    ],
)
