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
 * The {@link CMakeTask} prints environment variables and variables.
 */
public class CMakeTask extends TaskList {

  private final QtPlatform platform;
  private final String     moduleName;
  private final File       projectDir;


  private String target;

  /**
   * Creates an instance of {@link CMakeTask}.
   *
   * @param platform
   * @param moduleName
   * @param projectDir
   */
  public CMakeTask(QtPlatform platform, String moduleName, File projectDir) {
    this.platform = platform;
    this.moduleName = moduleName;
    this.projectDir = projectDir;
    this.target = null;
  }

  /**
   * Defines the CMake target.
   *
   * @param target
   */
  public CMakeTask setTarget(String target) {
    this.target = target;
    return this;
  }

  @Override
  protected final void collect(List<Task> tasks, TaskContext context) {
    String msvc_version = context.getEnvironment().get(Build.MSVC_VERSION);
    File buildPath = new File(context.getEnvironment().get(Build.BUILD_DIR), this.moduleName);

    if (this.platform.isAndroid()) {
      String suffix = (this.platform.getABI() == null) ? "" : "-" + this.platform.getABI();
      buildPath = new File(buildPath, this.platform.getArch(msvc_version) + suffix);
      tasks.add(new DeleteTask(buildPath));
      tasks.add(new CMakeAndroid(this.platform, buildPath));
    } else {
      buildPath = new File(buildPath, this.platform.getArch(msvc_version));
      if (target == null) {
        // Avoid delete on building
        tasks.add(new DeleteTask(buildPath));
      }
      tasks.add(new CMakeMain(this.platform, buildPath, target));
    }
  }

  /**
   * Creates a QMake process.
   */
  private class CMakeMain extends ProcessTask {

    protected final QtPlatform platform;
    private final File         buildDir;

    // For the CMAKE compiler on windows
    private final String target;

    /**
     * @param platform
     * @param buildDir
     * @param target
     */
    public CMakeMain(QtPlatform platform, File buildDir, String target) {
      this.platform = platform;
      this.buildDir = buildDir;
      this.target = target;
    }

    /**
     * Get the QMake shell command.
     */
    @Override
    protected CMakeBuilder getShellBuilder(TaskContext context) {
      File qtRoot = new File(context.getEnvironment().get(Build.QT_ROOT));
      File homeDir = new File(qtRoot, context.getEnvironment().get(Build.QT_VERSION));

      CMakeBuilder builder = new CMakeBuilder(CMakeTask.this.projectDir);
      builder.setBuildDir(buildDir);
      builder.setHome(homeDir);
      builder.setTarget(target);
      builder.setPlatform(this.platform);

      String qtConfig = context.getEnvironment().get(Build.QT_CONFIG);
      if (!qtConfig.isEmpty()) {
        Arrays.asList(qtConfig.split(",")).forEach(c -> builder.addConfig(c));
      }

      if (OS.isWindows()) {
        builder.setMsvcRoot(new File(context.getEnvironment().get(Build.MSVC_ROOT)));
        builder.setMsvcVersion(context.getEnvironment().get(Build.MSVC_VERSION));
      }

      return builder;
    }
  }

  /**
   * Creates a QMake process for android ABI's.
   */
  private class CMakeAndroid extends CMakeMain {

    /**
     * @param platform
     * @param buildDir
     */
    public CMakeAndroid(QtPlatform platform, File buildDir) {
      super(platform, buildDir, null);
    }

    /**
     * Get the QMake shell command for a android.
     */
    @Override
    protected final CMakeBuilder getShellBuilder(TaskContext context) {
      CMakeBuilder builder = super.getShellBuilder(context);
      builder.setEnvironment(Build.ANDROID_NDK_ROOT, context.getEnvironment().get(Build.ANDROID_NDK_ROOT));
      return builder;
    }
  }
}
