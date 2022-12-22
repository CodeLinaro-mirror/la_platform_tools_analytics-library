load("//tools/base/bazel:proto.bzl", "java_proto_library", "maven_proto_library")
load("//tools/base/common:version.bzl", "BASE_VERSION")

maven_proto_library(
    name = "proto",
    srcs = glob(["*.proto"]),
    coordinates = "com.android.tools.analytics-library:protos",
    description = "Library containing protobufs for usage analytics.",
    java_exports = ["@maven//:com.google.protobuf.protobuf-java"],
    notice = "NOTICE",
    pom_name = "Android Tools Analytics Protobufs",
    proto_java_runtime_library = [],
    resource_strip_prefix = "tools/analytics-library/protos/src/main/proto",
    resources = [
        "analytics_enums.proto",
        "google_logs_publishing.proto",
        "studio_stats.proto",
    ],
    version = BASE_VERSION,
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "studio.proto",
    srcs = glob(["*.proto"]),
    proto_java_runtime_library = ["@maven//:com.google.protobuf.protobuf-java"],
    visibility = ["//visibility:public"],
)
