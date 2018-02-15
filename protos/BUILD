load("//tools/base/bazel:bazel.bzl", "iml_module")

exports_files(["NOTICE"])

iml_module(
    name = "analytics-protos",
    srcs = ["src/main/java"],
    iml_files = ["analytics-protos.iml"],
    tags = ["managed"],
    visibility = ["//visibility:public"],
    exports = ["//tools/idea/.idea/libraries:protobuf"],
    deps = ["//tools/idea/.idea/libraries:protobuf"],
)
