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
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.util.env.OS;

/**
 * The {@link CMakeBuilder} class.
 */
public class CMakeBuilder extends CppBuilder {

  private File       home;
  private File       buildDir;
  private QtPlatform platform;
  private String     target;


  private final Set<String> config = new LinkedHashSet<>();

  /**
   * Constructs an instance of {@link CMakeBuilder}.
   *
   * @param workingDir
   */
  public CMakeBuilder(File workingDir) {
    super(workingDir);
    this.platform = QtPlatform.current();
  }

  /**
   * Sets the Qt home directory.
   */
  public final CMakeBuilder setHome(File home) {
    this.home = home;
    return this;
  }

  /**
   * Sets the CMake project directory.
   */
  public final CMakeBuilder setBuildDir(File buildDir) {
    this.buildDir = buildDir;
    return this;
  }

  /**
   * Sets the Qt platform.
   */
  public final CMakeBuilder setPlatform(QtPlatform platform) {
    this.platform = platform;
    return this;
  }

  /**
   * Adds a Qt configuration.
   */
  public final CMakeBuilder addConfig(String config) {
    this.config.add(config);
    return this;
  }


  /**
   * Sets the Qt platform.
   */
  public final CppBuilder setTarget(String target) {
    this.target = target;
    return this;
  }

  /**
   * Build the commands for the {@link CppBuilder}..
   */
  @Override
  protected void buildCommand(List<String> commands) {
    Path path = this.home.toPath().getParent().resolve("Tools");
    path = path.resolve(OS.isWindows() ? "CMake_64" : "CMake");
    if (OS.isMacOS()) {
      path = path.resolve("CMake.app").resolve("Contents");
    }
    path = path.resolve("bin").resolve(OS.isWindows() ? "cmake.exe" : "cmake");

    File cmake = path.toFile();
    if (!cmake.exists() || cmake.isDirectory()) {
      throw new RuntimeException("Invalid command: " + cmake);
    }

    commands.add(cmake.getAbsolutePath());

    if (target != null) {
      commands.add("--build");
      commands.add(this.buildDir.getAbsolutePath());
      commands.add("--target");
      commands.add(target);
      return;
    }

    commands.add("-B");
    commands.add(this.buildDir.getAbsolutePath());
    commands.add(".");

    Path qt_arch = home.toPath().resolve(platform.getArch(getMsvcVersion()));
    Path toolchain = qt_arch.resolve("lib").resolve("cmake").resolve("Qt6").resolve("qt.toolchain.cmake");

    commands.add("-DCMAKE_PREFIX_PATH:PATH=" + qt_arch.toString());
    commands.add("-DCMAKE_TOOLCHAIN_FILE:FILEPATH=" + toolchain.toString());

    switch (this.platform) {
      case IOS:
        commands.add("-DCMAKE_GENERATOR:STRING=Xcode");
        commands.add("-DCMAKE_OSX_ARCHITECTURES:STRING=arm64");
        commands.add("-DCMAKE_OSX_SYSROOT:STRING=iphoneos");
        break;

      case LINUX:
        commands.add("-DCMAKE_GENERATOR:STRING=\"Unix Makefiles\"");
        break;

      case WINDOWS:
        commands.add("-DCMAKE_GENERATOR:STRING=Ninja");
        break;

      default:
        break;
    }

    if ((this.platform == QtPlatform.ANDROID) && !this.platform.ABIs.isEmpty()) {
      commands.add(String.format("'%s=%s'", Build.ANDROID_ABIS, String.join(" ", this.platform.ABIs)));
    }
  }
}
