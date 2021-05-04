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

package it.smartio.task;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import it.smartio.common.env.Environment;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskLogger;


/**
 * The {@link TaskEnvironment} class.
 */
public class TaskEnvironment implements Environment, Closeable {

  private final TaskLogger          logger;
  private final Map<String, String> logged;
  private final Environment         environment;


  /**
   * Constructs an instance of {@link TaskEnvironment}.
   *
   * @param context
   */
  public TaskEnvironment(TaskContext context) {
    this.logger = context.getLogger();
    this.logged = new HashMap<>();
    this.environment = context.getEnvironment();
  }

  /**
   * Returns true if the environment contains the named variable.
   *
   * @param name
   */
  @Override
  public final boolean isSet(String name) {
    return this.environment.isSet(name);
  }

  /**
   * Returns the named environment variable, throws an exception otherwise.
   *
   * @param name
   */
  @Override
  public final String get(String name) {
    String value = this.environment.get(name);
    this.logged.put(name, value);
    return value;
  }

  /**
   * Converts the environment variables as {@link Map}.
   */
  @Override
  public final Map<String, String> toMap() {
    return this.environment.toMap();
  }

  @Override
  public void close() throws IOException {
    if (!this.logged.isEmpty()) {
      this.logger.onInfo(this.logged.entrySet().stream().map(e -> String.format("  %s = %s", e.getKey(), e.getValue()))
          .collect(Collectors.joining("\n")));
    }
  }
}
