load("//tools/base/bazel:proto.bzl", "java_proto_library", "maven_proto_library")
load("//tools/base/bazel:maven.bzl", "maven_java_library", "maven_pom")
load("//tools/base/common:version.bzl", "BASE_VERSION")

maven_proto_library(
    name = "proto_bzl",
    srcs = glob(["*.proto"]),
    coordinates = "com.android.tools.analytics-library:protos:" + BASE_VERSION,
    legacy_name = "proto",
    resource_strip_prefix = "tools/analytics-library/protos",
    resources = ["//tools/analytics-library/protos:NOTICE"],
    visibility = ["//visibility:public"],
)

java_proto_library(
    name = "studio.proto",
    srcs = glob(["*.proto"]),
    proto_java_runtime_library = ["//prebuilts/tools/common/m2:com.google.protobuf.protobuf-java.3.5.1"],
    protoc_version = "3.0.0",  # Should match the version used in Studio.
    visibility = ["//visibility:public"],
)

filegroup(
    name = "agp_gradle_build_files",
    srcs = glob(["*.proto"]),
    visibility = ["//tools/base/build-system:__pkg__"],
)
