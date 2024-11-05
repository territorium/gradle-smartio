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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;

import it.smartio.gradle.config.PipelineConfig;
import it.smartio.gradle.config.StageConfig;
import it.smartio.gradle.task.TaskApp;
import it.smartio.gradle.task.TaskEnv;
import it.smartio.gradle.task.TaskGit;
import it.smartio.gradle.task.TaskIpaUpload;
import it.smartio.gradle.task.TaskPipeline;
import it.smartio.gradle.task.TaskUnitTest;
import it.smartio.task.TaskDefinition;

/**
 * The {@link BuildPlugin} defines the different tasks required for a smart.IO build management.
 */
public class BuildPlugin implements Plugin<Project> {

  private static final String NAME_CONFIG = "smartIO";

  public static GradleConfig findConfig(Project project) {
    ExtensionContainer extension = project.getExtensions();
    return extension.findByType(GradleConfig.class);
  }

  @Override
  public void apply(Project project) {
    ExtensionContainer extension = project.getExtensions();
    GradleConfig config = extension.create(BuildPlugin.NAME_CONFIG, GradleConfig.class);

    project.getTasks().register("git", TaskGit.class);
    project.getTasks().register("env", TaskEnv.class);
    project.getTasks().register("run", TaskPipeline.class);

    appendPipeline(project, config);
    appendPipelineTasks(project, config);

    project.getTasks().register("branding", TaskApp.class);
    project.getTasks().register("unittest", TaskUnitTest.class);
    project.getTasks().register("ipa-upload", TaskIpaUpload.class);
  }

  /**
   * Install all pipeline related tasks. Parses the pipeline configuration
   *
   * @param project
   * @param config
   */
  protected void appendPipeline(Project project, GradleConfig config) {
    project.afterEvaluate(a -> {
      for (PipelineConfig p : config.getPipelines()) {
        for (StageConfig s : p.getStages()) {
          String taskName = String.format("run-%s-%s", p.name, s.name);
          Task task = project.task(taskName);
          task.setDescription(s.title);
          task.setGroup(String.format("smart.io pipeline '%s'", p.name));
          task.doLast(t -> new Pipeline(config, project).exec(p.name, s.name));
        }
      }
    });
  }

  /**
   * Install all pipeline related tasks. Parses the pipeline configuration
   *
   * @param project
   * @param config
   */
  protected void appendPipelineTasks(Project project, GradleConfig config) {
    project.afterEvaluate(a -> {
      for (TaskDefinition def : Pipeline.newFactory().getTasks()) {
        Task task = project.task("task-" + def.getName());
        task.setDescription(def.getDescription());
        task.setGroup("smart.io tasks");
        task.doLast(t -> project.getLogger().warn(def.getName()));
      }
    });
  }
}
