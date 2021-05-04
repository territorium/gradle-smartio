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

package it.smartio.task.shell;

import java.io.File;
import java.util.List;

import it.smartio.common.env.EnvironmentUtil;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.process.ProcessRequestBuilder;
import it.smartio.common.task.process.ProcessTask;
import it.smartio.gradle.Arguments;


/**
 * The {@link ShellTask} class.
 */
public class ShellTask extends ProcessTask {

  private final Arguments arguments;
  private final File      workingDir;

  /**
   * Constructs an instance of {@link ShellTask}.
   *
   * @param arguments
   * @param workingDir
   */
  public ShellTask(Arguments arguments, File workingDir) {
    this.arguments = arguments;
    this.workingDir = workingDir;
  }

  @Override
  protected final ProcessRequestBuilder getShellBuilder(TaskContext context) {
    List<String> commands = this.arguments.asList("command");
    File workingDir = new File(this.workingDir, this.arguments.get("workingDir"));

    // Replace environment variables
    for (int index = 0; index < commands.size(); index++) {
      commands.set(index, EnvironmentUtil.replace(commands.get(index), context.getEnvironment()));
    }

    return new ShellBuilder(commands, workingDir);
  }
}
