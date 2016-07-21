# This file has been automatically generated, please do not modify directly.
load("//tools/base/bazel:bazel.bzl", "kotlin_library", "groovy_library", "kotlin_groovy_library", "fileset")

java_library(
  name = "analytics-publisher",
  srcs = glob([
      "publisher/src/main/java/**/*.java",
    ]),
  resource_strip_prefix = "tools/analytics-library/analytics-publisher.resources",
  resources = [
      "//tools/analytics-library:analytics-publisher.res",
    ],
  deps = [
      "@local_jdk//:langtools-neverlink",
      "//tools/idea:lib/guava-18.0",
      "//tools/idea:lib/hamcrest-core-1.3",
      "//tools/idea:lib/junit-4.12",
      "//tools/base/annotations:android-annotations",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28-sources",
      "//tools/analytics-library:analytics-protos",
          "//tools/idea:lib/protobuf-2.5.0",
      "//tools/analytics-library:analytics-shared",
      "//tools/base/testutils:testutils",
          "//tools/base/common:common",
      "//tools/analytics-library:analytics-tracker",
    ],
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

java_library(
  name = "analytics-tracker",
  srcs = glob([
      "tracker/src/main/java/**/*.java",
    ]),
  resource_strip_prefix = "tools/analytics-library/analytics-tracker.resources",
  resources = [
      "//tools/analytics-library:analytics-tracker.res",
    ],
  deps = [
      "@local_jdk//:langtools-neverlink",
      "//tools/idea:lib/guava-18.0",
      "//tools/idea:lib/hamcrest-core-1.3",
      "//tools/idea:lib/junit-4.12",
      "//tools/base/annotations:android-annotations",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28-sources",
      "//tools/analytics-library:analytics-protos",
          "//tools/idea:lib/protobuf-2.5.0",
      "//tools/analytics-library:analytics-shared",
      "//tools/base/testutils:testutils",
          "//tools/base/common:common",
    ],
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

java_library(
  name = "analytics-tracker_testlib",
  srcs = glob([
      "tracker/src/test/java/**/*.java",
    ]),
  deps = [
      "@local_jdk//:langtools-neverlink",
      "//tools/analytics-library:analytics-tracker",
          "//tools/idea:lib/guava-18.0",
          "//tools/base/annotations:android-annotations",
      "//tools/idea:lib/hamcrest-core-1.3",
      "//tools/idea:lib/junit-4.12",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28-sources",
      "//tools/analytics-library:analytics-protos",
          "//tools/idea:lib/protobuf-2.5.0",
      "//tools/analytics-library:analytics-shared",
      "//tools/analytics-library:analytics-shared_testlib",
      "//tools/base/testutils:testutils",
          "//tools/base/common:common",
      "//tools/base/testutils:testutils_testlib",
          "//tools/base/common:common_testlib",
    ],
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

java_library(
  name = "analytics-shared",
  srcs = glob([
      "shared/src/main/java/**/*.java",
    ]),
  resource_strip_prefix = "tools/analytics-library/analytics-shared.resources",
  resources = [
      "//tools/analytics-library:analytics-shared.res",
    ],
  deps = [
      "@local_jdk//:langtools-neverlink",
      "//tools/idea:lib/guava-18.0",
      "//tools/idea:lib/hamcrest-core-1.3",
      "//tools/idea:lib/junit-4.12",
      "//tools/base/annotations:android-annotations",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28-sources",
      "//tools/idea:lib/gson-2.5",
      "//tools/analytics-library:analytics-protos",
          "//tools/idea:lib/protobuf-2.5.0",
      "//tools/base/testutils:testutils",
          "//tools/base/common:common",
    ],
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

java_test(
  name = "analytics-tracker_tests",
  srcs = glob([
    ]),
  runtime_deps = [
      ":analytics-tracker_testlib",
      "//tools/base/bazel:test_runner",
    ],
  jvm_flags = [
      "-Dtest.suite.jar=analytics-tracker_testlib.jar",
    ],
  test_class = "com.android.tools.BazelTestSuite",
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

fileset(
  name = "analytics-publisher.res",
  srcs = glob([
      "publisher/src/main/java/**/*",
    ],
    exclude = [
      "**/* *",
      "**/*.java",
      "**/*.kt",
      "**/*.groovy",
      "**/*$*",
      "**/.DS_Store",
    ]),
  mappings = {
      "publisher/src/main/java": "analytics-publisher.resources",
    },
  deps = [
      "@local_jdk//:langtools-neverlink",
    ],
)

java_library(
  name = "analytics-shared_testlib",
  srcs = glob([
      "shared/src/test/java/**/*.java",
    ]),
  deps = [
      "@local_jdk//:langtools-neverlink",
      "//tools/analytics-library:analytics-shared",
          "//tools/idea:lib/guava-18.0",
          "//tools/base/annotations:android-annotations",
      "//tools/idea:lib/hamcrest-core-1.3",
      "//tools/idea:lib/junit-4.12",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28-sources",
      "//tools/idea:lib/gson-2.5",
      "//tools/analytics-library:analytics-protos",
          "//tools/idea:lib/protobuf-2.5.0",
      "//tools/base/testutils:testutils",
          "//tools/base/common:common",
      "//tools/base/testutils:testutils_testlib",
          "//tools/base/common:common_testlib",
    ],
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

fileset(
  name = "analytics-shared.res",
  srcs = glob([
      "shared/src/main/java/**/*",
    ],
    exclude = [
      "**/* *",
      "**/*.java",
      "**/*.kt",
      "**/*.groovy",
      "**/*$*",
      "**/.DS_Store",
    ]),
  mappings = {
      "shared/src/main/java": "analytics-shared.resources",
    },
  deps = [
      "@local_jdk//:langtools-neverlink",
    ],
)

java_library(
  name = "analytics-protos",
  srcs = glob([
      "protos/src/main/java/**/*.java",
    ]),
  resource_strip_prefix = "tools/analytics-library/analytics-protos.resources",
  resources = [
      "//tools/analytics-library:analytics-protos.res",
    ],
  deps = [
      "@local_jdk//:langtools-neverlink",
      "//tools/idea:lib/protobuf-2.5.0",
    ],
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

java_test(
  name = "analytics-shared_tests",
  srcs = glob([
    ]),
  runtime_deps = [
      ":analytics-shared_testlib",
      "//tools/base/bazel:test_runner",
    ],
  jvm_flags = [
      "-Dtest.suite.jar=analytics-shared_testlib.jar",
    ],
  test_class = "com.android.tools.BazelTestSuite",
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

fileset(
  name = "analytics-protos.res",
  srcs = glob([
      "protos/src/main/java/**/*",
    ],
    exclude = [
      "**/* *",
      "**/*.java",
      "**/*.kt",
      "**/*.groovy",
      "**/*$*",
      "**/.DS_Store",
    ]),
  mappings = {
      "protos/src/main/java": "analytics-protos.resources",
    },
  deps = [
      "@local_jdk//:langtools-neverlink",
    ],
)

java_library(
  name = "analytics-publisher_testlib",
  srcs = glob([
      "publisher/src/test/java/**/*.java",
    ]),
  deps = [
      "@local_jdk//:langtools-neverlink",
      "//tools/analytics-library:analytics-publisher",
          "//tools/idea:lib/guava-18.0",
          "//tools/base/annotations:android-annotations",
      "//tools/idea:lib/hamcrest-core-1.3",
      "//tools/idea:lib/junit-4.12",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28",
      "//prebuilts/tools/common/m2:repository/com/google/truth/truth/0.28/truth-0.28-sources",
      "//tools/analytics-library:analytics-protos",
          "//tools/idea:lib/protobuf-2.5.0",
      "//tools/analytics-library:analytics-shared",
      "//tools/analytics-library:analytics-shared_testlib",
      "//tools/base/testutils:testutils",
          "//tools/base/common:common",
      "//tools/base/testutils:testutils_testlib",
          "//tools/base/common:common_testlib",
      "//tools/analytics-library:analytics-tracker",
      "//tools/analytics-library:analytics-tracker_testlib",
    ],
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)

fileset(
  name = "analytics-tracker.res",
  srcs = glob([
      "tracker/src/main/java/**/*",
    ],
    exclude = [
      "**/* *",
      "**/*.java",
      "**/*.kt",
      "**/*.groovy",
      "**/*$*",
      "**/.DS_Store",
    ]),
  mappings = {
      "tracker/src/main/java": "analytics-tracker.resources",
    },
  deps = [
      "@local_jdk//:langtools-neverlink",
    ],
)

java_test(
  name = "analytics-publisher_tests",
  srcs = glob([
    ]),
  runtime_deps = [
      ":analytics-publisher_testlib",
      "//tools/base/bazel:test_runner",
    ],
  jvm_flags = [
      "-Dtest.suite.jar=analytics-publisher_testlib.jar",
    ],
  test_class = "com.android.tools.BazelTestSuite",
  javacopts = ["-extra_checks:off"],
  visibility = ["//visibility:public"],
)
