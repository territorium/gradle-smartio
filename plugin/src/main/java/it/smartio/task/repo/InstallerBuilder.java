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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import it.smartio.common.task.process.ProcessRequest;
import it.smartio.common.task.process.ProcessRequestBuilder;
import it.smartio.util.env.OS;


/**
 * The {@link InstallerBuilder} class.
 */
public class InstallerBuilder extends ProcessRequestBuilder {

  private enum Mode {
    BOTH,
    ONLINE,
    OFFLINE;
  }


  private File   root;

  private Mode   mode;
  private File   config;
  private String target;

  private File   packages;


  private final Set<String> includes = new LinkedHashSet<>();
  private final Set<String> excludes = new LinkedHashSet<>();

  /**
   * Constructs an instance of {@link InstallerBuilder}.
   *
   * @param workingDir
   */
  public InstallerBuilder(File workingDir) {
    super(workingDir);
  }

  /**
   * Sets the Qt home directory.
   */
  public final InstallerBuilder setRoot(File root) {
    this.root = root;
    return this;
  }

  /**
   * Defines an online only installer
   */
  public final InstallerBuilder setOnlineOnly() {
    this.mode = Mode.ONLINE;
    return this;
  }

  /**
   * Defines an online only installer
   */
  public final InstallerBuilder setOfflineOnly() {
    this.mode = Mode.OFFLINE;
    return this;
  }

  /**
   * Sets the configuration file
   */
  public final InstallerBuilder setConfig(File config) {
    this.config = config;
    return this;
  }

  /**
   * Sets the configuration file
   */
  public final InstallerBuilder setTarget(String target) {
    this.target = target;
    return this;
  }

  /**
   * Sets the packages path
   */
  public final InstallerBuilder setPackages(File packages) {
    this.packages = packages;
    return this;
  }

  /**
   * Includes a module.
   */
  public final InstallerBuilder addInclude(String module) {
    this.includes.add(module);
    return this;
  }

  /**
   * Excludes a module.
   */
  public final InstallerBuilder addExclude(String module) {
    this.excludes.add(module);
    return this;
  }

  /**
   * Builds a {@link ProcessRequest}.
   */
  @Override
  public ProcessRequest build() {
    Path path = this.root.toPath().resolve("Tools/QtInstallerFramework");
    Optional<File> qtInstallerDir = Arrays.asList(path.toFile().listFiles()).stream().findFirst();
    if (!qtInstallerDir.isPresent()) {
      throw new IllegalAccessError("No Qt Installer Framework found");
    }
    path = qtInstallerDir.get().toPath().resolve("bin");

    List<String> commands = new ArrayList<>();

    // Create Installer
    commands.add(path.resolve(OS.isWindows() ? "binarycreator.exe" : "binarycreator").toString());

    // Set online/offline mode
    switch (this.mode) {
      case ONLINE:
        commands.add("-n");
        break;
      case OFFLINE:
        commands.add("-f");
        break;
      default:
    }

    commands.add("-c");
    commands.add(this.config.getAbsolutePath());
    commands.add(OS.isWindows() ? String.format("%s.exe", this.target) : this.target);


    // Add packages location
    File packages = this.packages;
    if (packages == null) {
      packages = new File(getWorkingDir(), "packages");
    }
    commands.add("-p");
    commands.add(packages.getAbsolutePath());

    // Define include modules
    if (!this.includes.isEmpty()) {
      commands.add("-i");
      commands.add(String.join(",", this.includes));
    }
    // Define exclude modules
    if (!this.excludes.isEmpty()) {
      commands.add("-i");
      commands.add(String.join(",", this.excludes));
    }

    return ProcessRequest.create(getWorkingDir(), commands);
  }
}
