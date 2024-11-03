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
import java.util.List;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskList;
import it.smartio.common.task.process.ProcessTask;
import it.smartio.util.env.OS;


/**
 * Defines a QMake task.
 */
public class MakeTask extends TaskList {

  protected final QtPlatform platform;
  protected final String     moduleName;
  private final String       command;

  /**
   * Creates an instance of {@link MakeTask}.
   *
   * @param platform
   * @param moduleName
   */
  public MakeTask(QtPlatform platform, String moduleName) {
    this(platform, moduleName, null);
  }


  /**
   * Creates an instance of {@link MakeTask}.
   *
   * @param platform
   * @param moduleName
   * @param command
   */
  public MakeTask(QtPlatform platform, String moduleName, String command) {
    this.command = command;
    this.platform = platform;
    this.moduleName = moduleName;
  }

  @Override
  protected void collect(List<Task> tasks, TaskContext context) {
    String msvc_version = context.getEnvironment().get(Build.MSVC_VERSION);
    File buildPath = new File(context.getEnvironment().get(Build.BUILD_DIR), this.moduleName);

    if (this.platform.isAndroid()) {
      buildPath = new File(buildPath, this.platform.getArch(msvc_version) + "-" + this.platform.getABI());
      tasks.add(new MakeShellTask(buildPath));
    } else {
      buildPath = new File(buildPath, this.platform.getArch(msvc_version));
      tasks.add(new MakeShellTask(buildPath));
    }
  }

  /**
   * Creates a QMake process.
   */
  protected class MakeShellTask extends ProcessTask {

    private final File buildDir;

    /**
     * @param buildDir
     */
    public MakeShellTask(File buildDir) {
      this.buildDir = buildDir;
    }

    /**
     * Get the QMake shell command.
     */
    @Override
    protected CppBuilder getShellBuilder(TaskContext context) {
      MakeBuilder builder = new MakeBuilder(this.buildDir);
      builder.setCommand(MakeTask.this.command);
      if (OS.isWindows()) {
        // For the JOM compiler on windows
        builder.setRoot(new File(context.getEnvironment().get(Build.QT_ROOT)));
        builder.setMsvcRoot(new File(context.getEnvironment().get(Build.MSVC_ROOT)));
        builder.setMsvcVersion(context.getEnvironment().get(Build.MSVC_VERSION));
      }
      return builder;
    }
  }
}