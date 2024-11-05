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

package it.smartio.gradle.task;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import it.smartio.gradle.GradleTask;
import it.smartio.task.unit.XTestSuiteTask;


/**
 * The {@link TaskUnitTest} class.
 */
public abstract class TaskUnitTest extends GradleTask {

  public TaskUnitTest() {
    super("Executes an XUnit Test!");
  }

  @Input
  @Option(option = "test", description = "Gets the test class to execute")
  public abstract Property<String> getTest();

  @TaskAction
  public void process() {
    exec(new XTestSuiteTask.XUnit(getTest().get()));
  }
}
