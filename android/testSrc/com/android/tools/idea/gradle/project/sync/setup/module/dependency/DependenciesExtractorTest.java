/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.gradle.project.sync.setup.module.dependency;

import static com.android.builder.model.level2.Library.LIBRARY_ANDROID;
import static com.android.builder.model.level2.Library.LIBRARY_JAVA;
import static com.android.tools.idea.gradle.project.sync.setup.module.dependency.DependenciesExtractor.getDependencyDisplayName;
import static com.google.common.truth.Truth.assertThat;
import static com.intellij.openapi.roots.DependencyScope.COMPILE;
import static com.intellij.openapi.util.io.FileUtil.join;
import static com.intellij.util.containers.ContainerUtil.getFirstItem;

import com.android.builder.model.level2.Library;
import com.android.ide.common.gradle.model.stubs.level2.AndroidLibraryStub;
import com.android.ide.common.gradle.model.stubs.level2.AndroidLibraryStubBuilder;
import com.android.ide.common.gradle.model.stubs.level2.IdeDependenciesStubBuilder;
import com.android.ide.common.gradle.model.stubs.level2.JavaLibraryStub;
import com.android.ide.common.gradle.model.stubs.level2.ModuleLibraryStub;
import com.android.ide.common.gradle.model.stubs.level2.ModuleLibraryStubBuilder;
import com.android.tools.idea.gradle.TestProjects;
import com.android.tools.idea.gradle.project.facet.gradle.GradleFacet;
import com.android.tools.idea.gradle.project.sync.setup.module.ModuleFinder;
import com.android.tools.idea.gradle.stubs.android.AndroidProjectStub;
import com.android.tools.idea.gradle.stubs.android.VariantStub;
import com.android.tools.idea.testing.Facets;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.module.Module;
import com.intellij.testFramework.PlatformTestCase;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link DependenciesExtractor}.
 */
public class DependenciesExtractorTest extends PlatformTestCase {
  private AndroidProjectStub myAndroidProject;
  private VariantStub myVariant;
  private ModuleFinder myModuleFinder;
  private DependenciesExtractor myDependenciesExtractor;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    myModuleFinder = new ModuleFinder(myProject);

    myAndroidProject = TestProjects.createBasicProject();
    myVariant = myAndroidProject.getFirstVariant();
    assertNotNull(myVariant);

    myDependenciesExtractor = new DependenciesExtractor();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (myAndroidProject != null) {
        myAndroidProject.dispose();
      }
    }
    finally {
      super.tearDown();
    }
  }

  public void testExtractFromJavaLibrary() {
    File jarFile = new File("~/repo/guava/guava-11.0.2.jar");
    Library javaLibrary = new JavaLibraryStub(LIBRARY_JAVA, "guava", jarFile);

    IdeDependenciesStubBuilder builder = new IdeDependenciesStubBuilder();
    builder.setJavaLibraries(ImmutableList.of(javaLibrary));
    myVariant.getMainArtifact().setLevel2Dependencies(builder.build());
    myVariant.getInstrumentTestArtifact().setLevel2Dependencies(builder.build());

    Collection<LibraryDependency> dependencies = myDependenciesExtractor.extractFrom(myVariant, myModuleFinder).onLibraries();
    assertThat(dependencies).hasSize(1);

    LibraryDependency dependency = getFirstItem(dependencies);
    assertNotNull(dependency);
    assertEquals("Gradle: guava", dependency.getName());
    // Make sure that is a "compile" dependency, even if specified as "test".
    assertEquals(COMPILE, dependency.getScope());

    File[] binaryPaths = dependency.getBinaryPaths();
    assertThat(binaryPaths).hasLength(1);
    assertEquals(jarFile, binaryPaths[0]);
  }

  public void testExtractFromAndroidLibraryWithLocalJar() {
    String rootDirPath = myAndroidProject.getRootDir().getPath();
    File libJar = new File(rootDirPath, join("bundle_aar", "androidLibrary.jar"));
    File libCompileJar = new File(rootDirPath, join("api.jar"));

    File resFolder = new File(rootDirPath, join("bundle_aar", "res"));
    File localJar = new File(rootDirPath, "local.jar");

    AndroidLibraryStubBuilder builder = new AndroidLibraryStubBuilder();
    builder.setArtifactAddress("com.android.support:support-core-ui:25.3.1@aar");
    builder.setJarFile(libJar.getPath());
    builder.setCompileJarFile(libCompileJar.getPath());
    builder.setResFolder(resFolder.getPath());
    builder.setLocalJars(Collections.singletonList(localJar.getPath()));
    AndroidLibraryStub library = builder.build();

    IdeDependenciesStubBuilder dependenciesStubBuilder = new IdeDependenciesStubBuilder();
    dependenciesStubBuilder.setAndroidLibraries(ImmutableList.of(library));
    myVariant.getMainArtifact().setLevel2Dependencies(dependenciesStubBuilder.build());
    myVariant.getInstrumentTestArtifact().setLevel2Dependencies(dependenciesStubBuilder.build());

    DependencySet dependencySet = myDependenciesExtractor.extractFrom(myVariant, myModuleFinder);
    List<LibraryDependency> dependencies = new ArrayList<>(dependencySet.onLibraries());
    assertThat(dependencies).hasSize(1);

    LibraryDependency dependency = dependencies.get(0);
    assertNotNull(dependency);
    assertEquals("Gradle: com.android.support:support-core-ui:25.3.1@aar", dependency.getName());

    File[] binaryPaths = dependency.getBinaryPaths();
    assertThat(binaryPaths).hasLength(3);
    assertThat(binaryPaths).asList().containsAllOf(localJar, libCompileJar, resFolder);
  }

  public void testExtractFromModuleDependency() {
    Module libModule = createModule("lib");
    GradleFacet gradleFacet = Facets.createAndAddGradleFacet(libModule);
    String gradlePath = ":lib";
    gradleFacet.getConfiguration().GRADLE_PROJECT_PATH = gradlePath;

    ModuleLibraryStubBuilder builder = new ModuleLibraryStubBuilder();
    builder.setProjectPath(gradlePath);
    ModuleLibraryStub library = builder.build();

    myModuleFinder = new ModuleFinder(myProject);
    myModuleFinder.addModule(libModule, ":lib");

    IdeDependenciesStubBuilder dependenciesStubBuilder = new IdeDependenciesStubBuilder();
    dependenciesStubBuilder.setModuleDependencies(ImmutableList.of(library));
    myVariant.getMainArtifact().setLevel2Dependencies(dependenciesStubBuilder.build());
    myVariant.getInstrumentTestArtifact().setLevel2Dependencies(dependenciesStubBuilder.build());
    Collection<ModuleDependency> dependencies = myDependenciesExtractor.extractFrom(myVariant, myModuleFinder).onModules();
    assertThat(dependencies).hasSize(1);

    ModuleDependency dependency = getFirstItem(dependencies);
    assertNotNull(dependency);
    assertEquals(gradlePath, dependency.getGradlePath());
    // Make sure that is a "compile" dependency, even if specified as "test".
    assertEquals(COMPILE, dependency.getScope());
    assertSame(libModule, dependency.getModule());

    LibraryDependency backup = dependency.getBackupDependency();
    assertNull(backup);
  }

  public void testGetDependencyDisplayName() {
    Library library1 = new JavaLibraryStub(LIBRARY_JAVA, "com.google.guava:guava:11.0.2@jar", new File(""));
    assertThat(getDependencyDisplayName(library1)).isEqualTo("guava:11.0.2");

    Library library2 = new JavaLibraryStub(LIBRARY_ANDROID, "android.arch.lifecycle:extensions:1.0.0-beta1@aar", new File(""));
    assertThat(getDependencyDisplayName(library2)).isEqualTo("lifecycle:extensions:1.0.0-beta1");

    Library library3 = new JavaLibraryStub(LIBRARY_ANDROID, "com.android.support.test.espresso:espresso-core:3.0.1@aar", new File(""));
    assertThat(getDependencyDisplayName(library3)).isEqualTo("espresso-core:3.0.1");

    Library library4 = new JavaLibraryStub(LIBRARY_JAVA, "foo:bar:1.0", new File(""));
    assertThat(getDependencyDisplayName(library4)).isEqualTo("foo:bar:1.0");
  }
}
