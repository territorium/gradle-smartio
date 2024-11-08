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
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import it.smartio.gradle.BuildPlugin;
import it.smartio.gradle.GradleConfig;
import it.smartio.gradle.GradleTask;
import it.smartio.gradle.Pipeline;


/**
 * The {@link TaskPipeline} class.
 */
public abstract class TaskPipeline extends GradleTask {

  public TaskPipeline() {
    super("Executes a pipeline!");
  }

  @Input
  @Option(option = "job", description = "Gets the pipeline name")
  public abstract Property<String> getJob();

  @Input
  @Optional
  @Option(option = "stage", description = "Gets the stage name")
  public abstract Property<String> getStage();

  @TaskAction
  public void process() {
    if (!getJob().isPresent()) {
      getProject().getLogger().warn("Missing parameter 'task'!");
      return;
    }

    GradleConfig config = BuildPlugin.findConfig(getProject());
    Pipeline pipeline = new Pipeline(config, getProject());
    pipeline.exec(getJob().get(), getStage().getOrNull());
  }
}
