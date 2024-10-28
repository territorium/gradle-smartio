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

package it.smartio.task;

import java.io.File;

import it.smartio.common.task.Task;
import it.smartio.gradle.Arguments;

/**
 * The {@link TaskDefinition} class.
 */
public class TaskDefinition {

  private final String      name;
  private final TaskCreator creator;

  private String            description;

  /**
   * Constructs an instance of {@link TaskDefinition}.
   *
   * @param name
   * @param creator
   */
  public TaskDefinition(String name, TaskCreator creator) {
    this.name = name;
    this.creator = creator;
  }

  /**
   * Gets the description.
   */
  public final String getName() {
    return name;
  }

  /**
   * Gets the description.
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Creates the Task.
   *
   * @param arguments
   * @param workingDir
   */
  protected final Task create(Arguments arguments, File workingDir) {
    return creator.create(arguments, workingDir);
  }

  /**
   * The {@link Builder} class.
   */
  public class Builder {

    public final void setDescription(String description) {
      TaskDefinition.this.description = description;
    }
  }
  /**
   * The {@link TaskCreator} the defines the lambda function to create a {@link Task}.
   */
  @FunctionalInterface
  public static interface TaskCreator {

    Task create(Arguments arguments, File workingDir);
  }
}
