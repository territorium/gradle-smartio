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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.smartio.common.task.Task;
import it.smartio.util.Builder;

/**
 * The {@link TaskBuilder} class.
 */
public class TaskBuilder implements Builder<Task> {

  private final String            name;
  private final Task              task;
  private final List<TaskBuilder> nodes = new ArrayList<>();

  /**
   * Constructs an instance of {@link TaskBuilder}.
   *
   * @param name
   */
  public TaskBuilder(String name) {
    this.name = name;
    this.task = null;
  }

  /**
   * Constructs an instance of {@link TaskBuilder}.
   *
   * @param name
   * @param task
   */
  private TaskBuilder(String name, Task task) {
    this.name = name;
    this.task = task;
  }

  /**
   * Adds a named {@link Task}.
   *
   * @param name
   */
  public final TaskBuilder addTask(String name) {
    TaskBuilder builder = new TaskBuilder(name);
    this.nodes.add(builder);
    return builder;
  }

  /**
   * Adds a named {@link Task}.
   *
   * @param name
   * @param task
   */
  public final TaskBuilder addTask(String name, Task task) {
    TaskBuilder builder = new TaskBuilder(name, task);
    this.nodes.add(builder);
    return builder;
  }

  /**
   * Builds an instance of {@link Task}.
   */
  @Override
  public final Task build() {
    return new TaskTree(name, task, nodes.stream().map(n -> (TaskTree) n.build()).collect(Collectors.toList()));
  }
}
