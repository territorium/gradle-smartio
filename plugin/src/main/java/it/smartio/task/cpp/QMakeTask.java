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
import java.util.Arrays;
import java.util.List;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskList;
import it.smartio.common.task.process.ProcessTask;
import it.smartio.task.file.DeleteTask;
import it.smartio.util.env.OS;


/**
 * The {@link QMakeTask} prints environment variables and variables.
 */
public class QMakeTask extends TaskList {

  private final QtPlatform platform;
  private final String     moduleName;
  private final File       projectFile;

  /**
   * Creates an instance of {@link QMakeTask}.
   *
   * @param platform
   * @param moduleName
   * @param projectFile
   */
  public QMakeTask(QtPlatform platform, String moduleName, File projectFile) {
    this.platform = platform;
    this.moduleName = moduleName;
    this.projectFile = projectFile;
  }


  @Override
  protected final void collect(List<Task> tasks, TaskContext context) {
    File buildPath = new File(context.getEnvironment().get(Build.BUILD_DIR), this.moduleName);

    if (this.platform.isAndroid()) {
      String suffix = (this.platform.abi == null) ? "" : "-" + this.platform.abi;
      buildPath = new File(buildPath, this.platform.arch + suffix);
      tasks.add(new DeleteTask(buildPath));
      tasks.add(new QMakeAndroid(this.platform, buildPath));
    } else {
      buildPath = new File(buildPath, this.platform.arch);
      tasks.add(new DeleteTask(buildPath));
      tasks.add(new QMakeMain(this.platform, buildPath));
    }
  }


  /**
   * Creates a QMake process.
   */
  private class QMakeMain extends ProcessTask {

    protected final QtPlatform platform;
    private final File         buildDir;

    /**
     * @param platform
     * @param buildDir
     * @param environment
     */
    public QMakeMain(QtPlatform platform, File buildDir) {
      this.platform = platform;
      this.buildDir = buildDir;
    }

    /**
     * Get the QMake shell command.
     */
    @Override
    protected QMakeBuilder getShellBuilder(TaskContext context) {
      File qtRoot = new File(context.getEnvironment().get(Build.QT_ROOT));
      File homeDir = new File(qtRoot, context.getEnvironment().get(Build.QT_VERSION));

      QMakeBuilder builder = new QMakeBuilder(this.buildDir);
      builder.setProjectFile(QMakeTask.this.projectFile);
      builder.setHome(homeDir);
      builder.setPlatform(this.platform);

      String qtConfig = context.getEnvironment().get(Build.QT_CONFIG);
      if (!qtConfig.isEmpty()) {
        Arrays.asList(qtConfig.split(",")).forEach(c -> builder.addConfig(c));
      }

      if (OS.isWindows()) {
        builder.setVcVarsAll(new File(context.getEnvironment().get(Build.VC_VARSALL)));
      }

      return builder;
    }
  }


  /**
   * Creates a QMake process for android ABI's.
   */
  private class QMakeAndroid extends QMakeMain {

    /**
     * @param platform
     * @param buildDir
     */
    public QMakeAndroid(QtPlatform platform, File buildDir) {
      super(platform, buildDir);
    }

    /**
     * Get the QMake shell command for a android.
     */
    @Override
    protected final QMakeBuilder getShellBuilder(TaskContext context) {
      QMakeBuilder builder = super.getShellBuilder(context);
      builder.setEnvironment(Build.ANDROID_NDK_ROOT, context.getEnvironment().get(Build.ANDROID_NDK_ROOT));
      return builder;
    }
  }
}
