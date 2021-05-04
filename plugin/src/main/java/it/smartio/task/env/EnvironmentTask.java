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

package it.smartio.task.env;

import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskLogger;


/**
 * The {@link EnvironmentTask} prints environment variables and variables.
 */
public class EnvironmentTask implements Task {

  /**
   * Prints the environment variables to the output.
   *
   * @param context
   */
  @Override
  public void handle(TaskContext context) {
    TaskLogger logger = context.getLogger();
    logger.onInfo("Platform:\t {}", System.getProperty("os.name"));
    logger.onInfo("Architecture:\t {}", System.getProperty("os.arch"));
    logger.onInfo("Version:\t {}", System.getProperty("os.version"));
    logger.onInfo("WorkingDir:\t {}", context.getWorkingDir().getAbsolutePath());
    logger.onInfo(context.getEnvironment().toString());
  }
}
