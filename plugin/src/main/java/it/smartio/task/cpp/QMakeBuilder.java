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

package it.smartio.task.cpp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.util.env.OS;


/**
 * The {@link QMakeBuilder} class.
 */
public class QMakeBuilder extends CppBuilder {

  private File              home;
  private File              project;
  private QtPlatform        platform;

  private final Set<String> config = new LinkedHashSet<>();


  private final String executable = OS.isWindows() ? "qmake.exe" : "qmake";

  /**
   * Constructs an instance of {@link QMakeBuilder}.
   *
   * @param workingDir
   */
  public QMakeBuilder(File workingDir) {
    super(workingDir);
    this.platform = QtPlatform.current();
  }

  /**
   * Sets the Qt home directory.
   */
  public final QMakeBuilder setHome(File home) {
    this.home = home;
    return this;
  }

  /**
   * Sets the Qt project file.
   */
  public final QMakeBuilder setProjectFile(File project) {
    this.project = project;
    return this;
  }

  /**
   * Sets the Qt platform.
   */
  public final QMakeBuilder setPlatform(QtPlatform platform) {
    this.platform = platform;
    return this;
  }

  /**
   * Adds a Qt configuration.
   */
  public final QMakeBuilder addConfig(String config) {
    this.config.add(config);
    return this;
  }

  /**
   * Build the commands for the {@link CppBuilder}..
   */
  @Override
  protected void buildCommand(List<String> commands) {
    File qmake = this.platform.toQtPath(this.home).toPath().resolve("bin").resolve(this.executable).toFile();
    if (!qmake.exists() || qmake.isDirectory()) {
      throw new RuntimeException("Invalid command: " + qmake);
    }

    commands.add(qmake.getAbsolutePath());
    commands.add("-spec");
    commands.add(this.platform.spec);

    if ((this.platform == QtPlatform.ANDROID) && !this.platform.ABIs.isEmpty()) {
      commands.add(String.format("'%s=%s'", Build.ANDROID_ABIS, String.join(" ", this.platform.ABIs)));
    }

    this.config.forEach(c -> commands.add("CONFIG+=" + c.trim()));

    commands.add(this.project.getAbsolutePath());
  }

  /**
   * Find the Qt project file for the working directory. Otherwise throws an {@link IOException}.
   *
   * @param projectDir
   */
  public static Optional<File> findProjectFile(File projectDir) {
    return Arrays.asList(projectDir.listFiles((d, n) -> n.endsWith(".pro"))).stream().findAny();
  }

  /**
   * Find the Qt project file for the working directory. Otherwise throws an {@link IOException}.
   *
   * @param workingDir
   */
  public static List<File> findProjects(File workingDir) {
    List<File> projectFiles = new ArrayList<>();
    for (File projectDir : workingDir.listFiles()) {
      if (!projectDir.isDirectory()) {
        continue;
      }

      Optional<File> file = QMakeBuilder.findProjectFile(projectDir);
      if (file.isPresent()) {
        projectFiles.add(file.get());
      }
    }
    return projectFiles;
  }
}
