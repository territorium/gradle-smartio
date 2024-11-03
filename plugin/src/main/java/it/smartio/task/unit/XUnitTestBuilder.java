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

package it.smartio.task.unit;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.env.Environment;
import it.smartio.common.task.process.ProcessRequest;
import it.smartio.common.task.process.ProcessRequestBuilder;
import it.smartio.util.env.OS;


/**
 * The {@link XUnitTestBuilder} class.
 */
public class XUnitTestBuilder extends ProcessRequestBuilder {

  private String unitTest;

  private File   targetDir;
  private File   qtHome;

  private File   msvcRoot;
  private String msvcVersion;

  /**
   * Constructs an instance of {@link XUnitTestBuilder}.
   *
   * @param workingDir
   */
  public XUnitTestBuilder(File workingDir) {
    super(workingDir);
  }

  /**
   * Sets the unit test.
   */
  public final XUnitTestBuilder setQtHome(File qtHome) {
    this.qtHome = qtHome;
    return this;
  }

  /**
   * Sets the unit test.
   */
  public final XUnitTestBuilder setTargetDir(File targetDir) {
    this.targetDir = targetDir;
    return this;
  }

  /**
   * Sets the unit test.
   */
  public final XUnitTestBuilder setUnitTest(String unitTest) {
    this.unitTest = unitTest;
    return this;
  }

  /**
   * Sets the MSVC VarsAll directory.
   */
  public final XUnitTestBuilder setMsvcRoot(File root) {
    this.msvcRoot = root;
    return this;
  }

  /**
   * Sets the MSVC VarsAll directory.
   */
  public final XUnitTestBuilder setMsvcVersion(String version) {
    this.msvcVersion = version;
    return this;
  }

  /**
   * Get the VisualCode Vars All to find the correct architecture.
   */
  protected final String getVcVarsAll() {
    Path path = msvcRoot.toPath().resolve(msvcVersion);
    path = path.resolve("BuildTools").resolve("VC").resolve("Auxiliary").resolve("Build");
    String script = path.resolve("vcvarsall.bat").toFile().getAbsolutePath();
    return script.contains(" ") ? "\"" + script + "\"" : script;
  }

  /**
   * Builds a {@link ProcessRequest}.
   */
  @Override
  public final ProcessRequest build() {
    if (this.unitTest == null) {
      throw new IllegalArgumentException("No unit test defined");
    }

    List<String> commands = new ArrayList<>();

    if (OS.isWindows()) {
      commands.add("cmd");
      commands.add("/c");
      commands.add(getVcVarsAll());
      commands.add("x86_amd64");
      commands.add("&");
    }

    QtPlatform arch = OS.isWindows() ? QtPlatform.WINDOWS : QtPlatform.LINUX;
    Path lib = this.targetDir.toPath().resolve(arch.getSpec()).resolve("lib");

    Map<String, String> environment = new HashMap<>();
    if (OS.isWindows()) {
      Path bin = this.qtHome.toPath().resolve(arch.getArch(msvcVersion)).resolve("bin");
      environment.put(Build.PATH_WIN64, lib.toAbsolutePath().toString() + ";" + bin.toAbsolutePath().toString());
    } else {
      environment.put(Build.LD_LIBRARY_PATH, lib.toAbsolutePath().toString());
    }

    Path test = this.targetDir.toPath().resolve(arch.getSpec()).resolve("bin");
    commands.add(test.resolve(OS.isWindows() ? this.unitTest + ".exe" : this.unitTest).toString());
    commands.add("-xunitxml");

    return ProcessRequest.create(getWorkingDir(), Environment.of(environment), commands);
  }
}
