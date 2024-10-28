/*
 * Copyright (c) 2001-2021 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.info/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package it.smartio.task.repo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.smartio.common.env.Environment;
import it.smartio.util.file.FileTreeCopying;

/**
 * The {@link PackageBuilder} is an utility class that creates the package structure for the
 * installer. The builder copies the meta data from a source directory and creates a build
 * directory.
 *
 * <pre>
 * -packages - com.vendor.root - data - meta - com.vendor.root.component1 - data - meta
 *     - com.vendor.root.component1.subcomponent1 - data - meta - com.vendor.root.component2 - data - meta
 * </pre>
 */
public class PackageBuilder {

  public static final String META = "meta";
  public static final String DATA = "data";


  private final File        workingDir;
  private final Environment environment;


  private File                    artifactsDir;
  private File                    packageDir;
  private final List<PackageData> packageData = new ArrayList<>();

  /**
   * Constructs an instance of {@link PackageBuilder} for the working directory.
   *
   * @param workingDir
   * @param environment
   */
  public PackageBuilder(File workingDir, Environment environment) {
    this.workingDir = workingDir;
    this.environment = environment;
  }


  /**
   * Gets the working directory.
   */
  protected final File getWorkingDir() {
    return this.workingDir;
  }


  /**
   * Gets the {@link Environment}.
   */
  protected final Environment getEnvironment() {
    return this.environment;
  }


  /**
   * Gets the artifact directory.
   */
  protected final File getArtifactsDir() {
    return this.artifactsDir;
  }


  /**
   * Gets the package directory.
   */
  protected final File getPackageDir() {
    return this.packageDir;
  }

  /**
   * Get the list of {@link PackageData}.
   */
  protected final List<PackageData> packageData() {
    return this.packageData;
  }

  /**
   * Set the artifacts directory.
   *
   * @param artifactsDir
   */
  public final void setArtifactsDir(File artifactsDir) {
    this.artifactsDir = artifactsDir;
  }

  /**
   * Set the package directory.
   *
   * @param packageDir
   */
  public final void setPackageDir(File packageDir) {
    this.packageDir = packageDir;
  }

  /**
   * Add a package with the location of the package data.
   *
   * @param name
   * @param source
   * @param target
   */
  public void addPackage(String name, String source, String target) {
    this.packageData.add(new PackageData(name, source, target, this));
  }

  /**
   * Copy all package definitions of the module and its dependencies.
   *
   * @param data
   */
  private void buildDependencies(PackageData data) throws IOException {
    Map<String, String> modules = new HashMap<>();

    // Collect depending packages
    for (File file : getWorkingDir().listFiles()) {
      String moduleName = data.remap(file.getName());
      File location = new File(getPackageDir(), moduleName);
      if (data.getName().contains(moduleName) && !location.exists()) {
        modules.put(file.getName(), moduleName);
      }
    }

    // Process defined packages
    for (Map.Entry<String, String> module : modules.entrySet()) {
      Path sourcePath = getWorkingDir().toPath().resolve(module.getKey());
      Path targetPath = getPackageDir().toPath().resolve(module.getValue());

      targetPath.toFile().mkdirs();
      FileTreeCopying.copyFileTree(sourcePath, targetPath);
      File meta = new File(targetPath.toFile(), PackageBuilder.META);
      for (File file : meta.listFiles()) {
        String content = new String(Files.readAllBytes(file.toPath()));
        try (Writer writer = new FileWriter(file)) {
          writer.write(data.remap(content));
        }
      }
    }
  }

  /**
   * Build a package structure for the {@link PackageBuilder}. The method expects a source
   * file/folder, the Version information and the relative installation path.
   *
   * Update the meta/package.xml with the actual version and release date.
   */
  public final void build() throws IOException {
    for (PackageData data : packageData()) {
      buildDependencies(data);
    }

    for (PackageData data : packageData()) {
      data.build(getPackageDir(), getEnvironment());
    }
  }
}
