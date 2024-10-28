/*
 * Copyright (c) 2001-2024 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.gradle;

import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;

import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;
import it.smartio.task.git.GitTask;

/**
 * The {@link TaskExecutor} class.
 */
public interface TaskExecutor {

  /**
   * Executes a {@link Task}.
   *
   * @param task
   * @param config
   */
  static void exec(Task task, GradleConfig config) {
    File workingDir = config.getWorkingDir();
    Environment environment = Environment.system();
    Logger logger = config.getProject().getLogger();

    logger.warn("WorkingDir: '{}'", workingDir);
    logger.warn("Loading environment variables...");
    try {
      environment = GradleEnvironment.parse(config, workingDir, environment);
    } catch (Throwable e) {
      logger.error("Couldn't load environment variables!", e);
      throw new RuntimeException(e);
    }
    logger.warn("Environment variables loaded!");

    GradleContext context = new GradleContext(logger, workingDir, environment);
    try {
      task.handle(context);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Executes a {@link GitTask}.
   *
   * @param task
   * @param config
   */
  static void execGit(GitTask task, GradleConfig config) {
    Logger logger = config.getProject().getLogger();
    Environment environment = Environment.of(config.toGitEnv());
    task.handle(new GradleContext(logger, config.getWorkingDir(), environment));
  }
}
