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
 * The {@link RepositoryBuilder} class.
 */
public class RepositoryBuilder extends ProcessRequestBuilder {

  private File    root;

  private boolean update;
  private File    repository;


  private final Set<String> includes = new LinkedHashSet<>();
  private final Set<String> excludes = new LinkedHashSet<>();

  /**
   * Constructs an instance of {@link RepositoryBuilder}.
   *
   * @param workingDir
   */
  public RepositoryBuilder(File workingDir) {
    super(workingDir);
  }

  /**
   * Get the Qt root directory
   */
  protected final File getQtRoot() {
    return this.root;
  }

  /**
   * Get the repository directory
   */
  protected final File getRepository() {
    return this.repository;
  }

  /**
   * Sets the Qt home directory.
   */
  public final RepositoryBuilder setQtRoot(File root) {
    this.root = root;
    return this;
  }

  /**
   * Sets the configuration file
   */
  public final RepositoryBuilder setUpdateOnly() {
    this.update = true;
    return this;
  }

  /**
   * Sets the repository path
   */
  public final RepositoryBuilder setRepository(File repository) {
    this.repository = repository;
    return this;
  }

  /**
   * Includes a module.
   */
  public final RepositoryBuilder addInclude(String module) {
    this.includes.add(module);
    return this;
  }

  /**
   * Excludes a module.
   */
  public final RepositoryBuilder addExclude(String module) {
    this.excludes.add(module);
    return this;
  }

  /**
   * Builds a {@link ProcessRequest}.
   */
  @Override
  public ProcessRequest build() {
    Path path = getQtRoot().toPath().resolve("Tools/QtInstallerFramework");
    Optional<File> qtInstallerDir = Arrays.asList(path.toFile().listFiles()).stream().findFirst();
    if (!qtInstallerDir.isPresent()) {
      throw new IllegalAccessError("No Qt Installer Framework found");
    }
    path = qtInstallerDir.get().toPath().resolve("bin");

    List<String> commands = new ArrayList<>();
    commands.add(path.resolve(OS.isWindows() ? "repogen.exe" : "repogen").toString());

    if (this.update) {
      commands.add("--update");
    }

    // Add packages location
    commands.add("-p");
    commands.add(".");

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

    commands.add(getRepository().getAbsolutePath());

    return ProcessRequest.create(getWorkingDir(), commands);
  }
}
