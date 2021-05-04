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

package it.smartio.common.task;

import java.io.File;

import it.smartio.common.env.Environment;

/**
 * The {@link TaskContext} class.
 */
public interface TaskContext {

  /**
   * Gets the working directory.
   */
  File getWorkingDir();

  /**
   * Gets the environment variables.
   */
  Environment getEnvironment();

  /**
   * Gets the task logger.
   */
  TaskLogger getLogger();

  /**
   * Creates a wrapped {@link TaskContext}.
   *
   * @param environment
   */
  default TaskContext wrap(Environment environment) {
    return TaskContext.wrap(getWorkingDir(), environment, getLogger());
  }

  /**
   * Creates a wrapped {@link TaskContext}.
   *
   * @param workingDir
   * @param environment
   */
  default TaskContext wrap(File workingDir, Environment environment) {
    return TaskContext.wrap(workingDir, environment, getLogger());
  }

  /**
   * Creates a wrapped {@link TaskContext}.
   *
   * @param workingDir
   * @param environment
   * @param logger
   */
  static TaskContext wrap(File workingDir, Environment environment, TaskLogger logger) {
    return new TaskContext() {

      @Override
      public final File getWorkingDir() {
        return workingDir;
      }

      @Override
      public final Environment getEnvironment() {
        return environment;
      }

      @Override
      public final TaskLogger getLogger() {
        return logger;
      }
    };
  }
}
