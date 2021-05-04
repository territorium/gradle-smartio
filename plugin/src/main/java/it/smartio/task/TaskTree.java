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

import java.util.List;

import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;

/**
 * The {@link TaskTree} class.
 */
public class TaskTree implements Task {

  private final String         name;
  private final Task           task;
  private final List<TaskTree> nodes;

  /**
   * Constructs an instance of {@link TaskTree}.
   *
   * @param name
   * @param task
   */
  public TaskTree(String name, Task task, List<TaskTree> nodes) {
    this.name = name;
    this.task = task;
    this.nodes = nodes;
  }

  /**
   * Executes the {@link Task}.
   *
   * @param context
   */
  @Override
  public void handle(TaskContext context) {
    if (this.task != null) {
      context.getLogger().onInfo("Task '{}': starting...", this.name);
    }
    try {
      if (this.task != null) {
        try (TaskEnvironment env = new TaskEnvironment(context)) {
          this.task.handle(context.wrap(env));
        }
      }

      this.nodes.forEach(n -> n.handle(context));

      if (this.task != null) {
        context.getLogger().onInfo("Task '{}': completed!", this.name);
      }
    } catch (Throwable e) {
      if (this.task != null) {
        context.getLogger().onError(e, "Task '{}': terminated!", this.name);
      }
      throw new RuntimeException(e);
    }
  }
}
