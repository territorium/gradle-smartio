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

package it.smartio.common.task.process;

import java.io.File;
import java.util.List;

import it.smartio.common.env.Environment;

/**
 * The {@link ProcessRequest} class.
 */
public class ProcessRequest {

  private final File         workingDir;
  private final Environment  environment;
  private final List<String> commands;

  /**
   * Constructs an instance of {@link ProcessRequest}.
   *
   * @param workingDir
   * @param environment
   * @param commands
   */
  private ProcessRequest(File workingDir, Environment environment, List<String> commands) {
    this.workingDir = workingDir;
    this.environment = environment;
    this.commands = commands;
  }

  /**
   * Get the working directory
   */
  public final File getWorkingDir() {
    return this.workingDir;
  }

  /**
   * Get the environment variables
   */
  public final Environment getEnvironment() {
    return this.environment;
  }

  /**
   * Get the command line parameters to execute the {@link Process}.
   */
  public final List<String> getCommand() {
    return this.commands;
  }

  /**
   * Creates a default instance.
   *
   * @param workingDir
   * @param commands
   */
  public static ProcessRequest create(File workingDir, List<String> commands) {
    return new ProcessRequest(workingDir, Environment.empty(), commands);
  }

  /**
   * Creates a default instance.
   *
   * @param workingDir
   * @param environment
   * @param commands
   */
  public static ProcessRequest create(File workingDir, Environment environment, List<String> commands) {
    return new ProcessRequest(workingDir, environment, commands);
  }
}