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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.smartio.common.env.Environment;
import it.smartio.common.task.process.ProcessRequest;
import it.smartio.common.task.process.ProcessRequestBuilder;
import it.smartio.util.env.OS;

/**
 * The {@link CppBuilder} class.
 */
public abstract class CppBuilder extends ProcessRequestBuilder {

  private final Map<String, String> environment;


  private File   msvcRoot;
  private String msvcVersion;

  /**
   * Constructs an instance of {@link CppBuilder}.
   *
   * @param workingDir
   */
  protected CppBuilder(File workingDir) {
    super(workingDir);
    this.environment = new HashMap<>();
  }

  /**
   * Sets the MSVC VarsAll directory.
   */
  public final CppBuilder setMsvcRoot(File root) {
    this.msvcRoot = root;
    return this;
  }

  /**
   * Sets the MSVC VarsAll directory.
   */
  public final CppBuilder setMsvcVersion(String version) {
    this.msvcVersion = version;
    return this;
  }

  /**
   * Sets an environment variable.
   *
   * @param name
   * @param value
   */
  public final CppBuilder setEnvironment(String name, String value) {
    this.environment.put(name, value);
    return this;
  }

  /**
   * Build the commands for the {@link CppBuilder}..
   */
  protected abstract void buildCommand(List<String> commands);

  /**
   * Get the VisualCode Vars All to find the correct architecture.
   */
  protected final String getMsvcVersion() {
    return msvcVersion;
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
    List<String> commands = new ArrayList<>();

    if (OS.isWindows()) {
      commands.add(getVcVarsAll());
      commands.add("x86_amd64");
      commands.add("&");
      commands.add("cmd");
      commands.add("/c");
    } else {
      commands.addAll(Arrays.asList("sh", "-c"));
    }

    List<String> arguments = new ArrayList<>();
    buildCommand(arguments);
    commands.add(String.join(" ", arguments));

    return ProcessRequest.create(getWorkingDir(), Environment.of(this.environment), commands);
  }
}
