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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.smartio.util.env.OS;


/**
 * The {@link MakeBuilder} class.
 */
public class MakeBuilder extends CppBuilder {

  private File                      root;
  private String                    command;

  private final Map<String, String> options = new HashMap<>();

  /**
   * Constructs an instance of {@link MakeBuilder}.
   *
   * @param workingDir
   */
  public MakeBuilder(File workingDir) {
    super(workingDir);
  }

  /**
   * Sets the Qt root directory.
   */
  public final MakeBuilder setRoot(File root) {
    this.root = root;
    return this;
  }

  /**
   * Sets the make command.
   */
  public final MakeBuilder setCommand(String command) {
    this.command = command;
    return this;
  }

  /**
   * Sets a command option.
   */
  public final MakeBuilder setOption(String name, String value) {
    this.options.put(name, value);
    return this;
  }

  /**
   * Build the commands for the {@link CppBuilder}..
   */
  @Override
  protected void buildCommand(List<String> commands) {
    if (OS.isWindows()) {
      File jom = this.root.toPath().resolve("Tools/QtCreator/bin/jom/jom.exe").toFile();
      if (!jom.exists() || jom.isDirectory()) {
        throw new RuntimeException("Invalid command: " + jom);
      }
      commands.add(jom.getAbsolutePath());
    } else {
      commands.add("make");
    }
    commands.add("-j8");

    if (this.command != null) {
      commands.add(this.command);
    }

    for (Map.Entry<String, String> option : this.options.entrySet()) {
      commands.add(String.format("%s=%s", option.getKey(), option.getValue()));
    }
  }
}
