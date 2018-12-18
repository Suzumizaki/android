load("//tools/base/bazel:bazel.bzl", "iml_module")

# managed by go/iml_to_build
iml_module(
    name = "intellij.android.databinding",
    # do not sort: must match IML order
    srcs = ["src"],
    iml_files = ["intellij.android.databinding.iml"],
    test_resources = ["testData"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    runtime_deps = [
        "//tools/idea/xml/relaxng:intellij.relaxng",
        "//tools/idea/platform/tasks-platform-impl:intellij.platform.tasks.impl",
        "//tools/idea/json:intellij.json",
        "//tools/idea/platform/credential-store:intellij.platform.credentialStore",
        "//tools/idea/jvm/jvm-analysis-impl:intellij.jvm.analysis.impl",
        "//tools/idea/java/compiler/instrumentation-util-8:intellij.java.compiler.instrumentationUtil.java8",
        "//tools/idea/plugins/maven/error-prone-compiler:intellij.maven.errorProne.compiler",
        "//tools/idea/plugins/IntelliLang/intellilang-jps-plugin:intellij.java.langInjection.jps",
        "//tools/adt/idea/adt-branding:intellij.android.adt.branding",
    ],
    # do not sort: must match IML order
    deps = [
        "//tools/idea/platform/core-api:intellij.platform.core[module]",
        "//tools/idea/java/java-psi-impl:intellij.java.psi.impl[module]",
        "//tools/idea/java/java-indexing-api:intellij.java.indexing[module]",
        "//tools/adt/idea/android:intellij.android.core[module]",
        "//tools/idea/platform/projectModel-api:intellij.platform.projectModel[module]",
        "//tools/idea/platform/lang-api:intellij.platform.lang[module]",
        "//tools/idea/xml/xml-psi-api:intellij.xml.psi[module]",
        "//tools/idea/xml/dom-openapi:intellij.xml.dom[module]",
        "//tools/idea/xml/dom-impl:intellij.xml.dom.impl[module]",
        "//tools/idea/platform/lang-impl:intellij.platform.lang.impl[module]",
        "//tools/idea/xml/xml-psi-impl:intellij.xml.psi.impl[module]",
        "//tools/idea/xml/impl:intellij.xml.impl[module]",
    ],
)

# managed by go/iml_to_build
iml_module(
    name = "intellij.android.databinding.tests",
    iml_files = ["intellij.android.databinding.tests.iml"],
    tags = [
        "no_test_mac",
        "no_test_mac_psq",  # b/113099009
    ],
    test_class = "com.android.tools.idea.databinding.AndroidDataBindingTestSuite",
    test_data = [
        "//prebuilts/tools/common/kotlin-plugin:kotlin-plugin-for-studio",
        "//prebuilts/studio/jdk",
        "//prebuilts/studio/sdk:build-tools/latest",
        "//prebuilts/studio/sdk:platforms/latest",
        "//tools/adt/idea/android:test_deps.zip",
        "//tools/adt/idea/android/annotations",
        "//tools/adt/idea/databinding/testData",
        "//tools/base/build-system:gradle-distrib",
        "//tools/base/build-system:studio_repo.zip",
        "//tools/base/templates",
        "//tools/data-binding:data_binding_runtime.zip",
        "//tools/idea/java/jdkAnnotations",
        "//tools/idea/java/mockJDK-1.7",
    ],
    test_srcs = ["testSrc"],
    visibility = ["//visibility:public"],
    # do not sort: must match IML order
    runtime_deps = [
        "//tools/idea/xml/relaxng:intellij.relaxng",
        "//tools/idea/platform/tasks-platform-impl:intellij.platform.tasks.impl",
        "//tools/idea/json:intellij.json",
        "//tools/idea/platform/credential-store:intellij.platform.credentialStore",
        "//tools/idea/jvm/jvm-analysis-impl:intellij.jvm.analysis.impl",
        "//tools/idea/java/compiler/instrumentation-util-8:intellij.java.compiler.instrumentationUtil.java8",
        "//tools/idea/plugins/maven/error-prone-compiler:intellij.maven.errorProne.compiler",
        "//tools/idea/plugins/IntelliLang/intellilang-jps-plugin:intellij.java.langInjection.jps",
        "//tools/adt/idea/adt-branding:intellij.android.adt.branding",
    ],
    # do not sort: must match IML order
    deps = [
        "//tools/idea/.idea/libraries:JUnit4[test]",
        "//tools/idea/.idea/libraries:commons-io[test]",
        "//tools/base/testutils:studio.android.sdktools.testutils[module, test]",
        "//tools/adt/idea/adt-testutils:intellij.android.adt.testutils[module, test]",
        "//tools/adt/idea/android:intellij.android.core[module, test]",
        "//tools/adt/idea/android-kotlin:intellij.android.kotlin.extensions[module, test]",
        "//tools/adt/idea/android-test-framework:intellij.android.testFramework[module, test]",
        "//tools/idea:intellij.platform.jps.build_and_others[module, test]",
        "//tools/idea/platform/editor-ui-api:intellij.platform.editor[module, test]",
        "//tools/idea/platform/testFramework:intellij.platform.testFramework[module, test]",
        "//tools/idea/.idea/libraries:truth[test]",
        "//tools/adt/idea/databinding:intellij.android.databinding[module]",
    ],
)