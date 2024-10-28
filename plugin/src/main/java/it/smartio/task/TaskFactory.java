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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import it.smartio.common.task.Task;
import it.smartio.gradle.Arguments;
import it.smartio.task.TaskDefinition.Builder;
import it.smartio.task.TaskDefinition.TaskCreator;

/**
 * The {@link TaskFactory} implements a factory to create instances of tasks from the arguments.
 */
public class TaskFactory {

  private final Map<String, TaskDefinition> definitions = new HashMap<>();

  /**
   * Add a {@link TaskDefinition}.
   *
   * @param name
   * @param constructor
   */
  public final Builder add(String name, TaskCreator constructor) {
    TaskDefinition definition = new TaskDefinition(name, constructor);
    this.definitions.put(name, definition);
    return definition.new Builder();
  }

  /**
   * Get the collection of task definitions.
   */
  public final Collection<TaskDefinition> getTasks() {
    return this.definitions.values();
  }

  /**
   * Creates an instance of an {@link Task}.
   *
   * @param name
   * @param arguments
   * @param workingDir
   */
  public final Task createTask(String name, Arguments arguments, File workingDir) {
    if (this.definitions.containsKey(name)) {
      return this.definitions.get(name).create(arguments, workingDir);
    } else {
      return c -> c.getLogger().onInfo("Task '{}' not found!", name);
    }
  }
}
