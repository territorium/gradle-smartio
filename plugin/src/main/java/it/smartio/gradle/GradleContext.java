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

package it.smartio.gradle;

import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.InputStream;

import it.smartio.common.env.Environment;
import it.smartio.common.task.TaskContextAsync;
import it.smartio.common.task.TaskLogger;

/**
 * The {@link GradleContext} class.
 */
public class GradleContext extends TaskContextAsync {

  private final TaskLogger logger;

  /**
   * Constructs an instance of {@link GradleContext}.
   *
   * @param logger
   * @param workingDir
   * @param environment
   */
  public GradleContext(Logger logger, File workingDir, Environment environment) {
    super(workingDir, environment);
    this.logger = new GradleLogger(logger);
  }

  /**
   * Gets the {@link TaskLogger}.
   */
  @Override
  public final TaskLogger getLogger() {
    return this.logger;
  }

  private class GradleLogger implements TaskLogger {

    private final Logger logger;

    private GradleLogger(Logger logger) {
      this.logger = logger;
    }

    @Override
    public void onRedirect(InputStream input, InputStream error) {
      redirectStreams(input, error);
    }

    @Override
    public void onInfo(String message, Object... arguments) {
      this.logger.warn(message, arguments);
    }

    @Override
    public void onError(Throwable throwable, String message, Object... arguments) {
      if (arguments.length == 0) {
        this.logger.error(message, throwable);
      } else {
        this.logger.error(message, arguments);
      }
    }
  }
}
