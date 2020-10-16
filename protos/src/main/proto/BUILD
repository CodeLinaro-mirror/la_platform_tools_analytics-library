load("//tools/base/bazel:proto.bzl", "java_proto_library")
load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")

java_proto_library(
    name = "proto",
    srcs = glob(["*.proto"]),
    pom = ":analytics-protos.pom",
    resource_strip_prefix = "tools/analytics-library/protos",
    resources = ["//tools/analytics-library/protos:NOTICE"],
    visibility = ["//visibility:public"],
)

maven_pom(
    name = "analytics-protos.pom",
    artifact = "protos",
    group = "com.android.tools.analytics-library",
    source = "//tools/buildSrc/base:base_version",
)

java_proto_library(
    name = "studio.proto",
    srcs = glob(["*.proto"]),
    proto_java_runtime_library = ["//prebuilts/tools/common/m2/repository/com/google/protobuf/protobuf-java/3.5.1:jar"],
    protoc_version = "3.0.0",  # Should match the version used in Studio.
    visibility = ["//visibility:public"],
)
