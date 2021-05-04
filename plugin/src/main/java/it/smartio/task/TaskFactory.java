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

import java.io.File;
import java.util.Map;
import java.util.Set;

import it.smartio.common.task.Task;
import it.smartio.gradle.Arguments;

/**
 * The {@link TaskFactory} implements a factory to create instances of tasks from the arguments.
 */
public class TaskFactory {

  private final Map<String, Constructor> constructors;

  /**
   * Constructs an instance of {@link TaskFactory}.
   *
   * @param constructors
   */
  protected TaskFactory(Map<String, Constructor> constructors) {
    this.constructors = constructors;
  }

  /**
   * Create the set of {@link Task}'s.
   */
  public final Set<String> getTasks() {
    return this.constructors.keySet();
  }

  /**
   * Creates an instance of an {@link Task}.
   *
   * @param name
   * @param arguments
   * @param workingDir
   */
  public final Task createTask(String name, Arguments arguments, File workingDir) {
    if (this.constructors.containsKey(name)) {
      return this.constructors.get(name).create(arguments, workingDir);
    } else {
      return c -> c.getLogger().onInfo("Task '{}' not found!", name);
    }
  }

  /**
   * The {@link Constructor} the defines the lambda function to create a {@link Task}.
   */
  @FunctionalInterface
  protected static interface Constructor {

    Task create(Arguments arguments, File workingDir);
  }
}
