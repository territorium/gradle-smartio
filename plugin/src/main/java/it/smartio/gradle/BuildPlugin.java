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
import org.gradle.api.plugins.ExtensionContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.smartio.gradle.config.PipelineConfig;
import it.smartio.gradle.config.StageConfig;
import it.smartio.task.env.EnvironmentTask;
import it.smartio.task.git.Git;
import it.smartio.task.git.GitTask;
import it.smartio.task.git.GitTaskCheckout;
import it.smartio.task.git.GitTaskPull;
import it.smartio.task.git.GitTaskResetHard;
import it.smartio.task.git.GitTaskTag;
import it.smartio.task.git.GitTaskVersion;
import it.smartio.task.product.BrandingTask;
import it.smartio.task.unit.XTestSuiteTask;
import it.smartio.task.xcode.IPAUploadTask;

/**
 * The {@link BuildPlugin} defines the different tasks required for a smart.IO build management.
 */
public class BuildPlugin implements Plugin<Project> {

  private static final String NAME_CONFIG = "smartIO";

  @Override
  public void apply(Project project) {
    ExtensionContainer extension = project.getExtensions();
    GradleConfig config = extension.create(BuildPlugin.NAME_CONFIG, GradleConfig.class);

    // Tasks
    project.task("env").doLast(t -> TaskExecutor.exec(new EnvironmentTask(), config))
        .setDescription("Shows the current environment!");

    appendGit(project, config);
    appendBuild(project, config);
    appendPipeline(project, config);
    appendPipelineTasks(project, config);

    project.task("unittest")
        .doLast(t -> TaskExecutor.exec(new XTestSuiteTask.XUnit(config.getParameter("test")), config));

    // TODO
    project.task("branding").doLast(t -> {
      Arguments args = new Arguments(config.getProject().getProperties());
      TaskExecutor.exec(new BrandingTask(args.get("source"), args.get("target")), config);
    }).setDescription("Creates the branding from a build.properties!");

    project.task("ipa-upload")
        .doLast(t -> TaskExecutor
            .exec(new IPAUploadTask("smartIO", "app", (String) t.getProject().getProperties().get("artifact")), config))
        .setDescription("Executes a pipeline!");
  }

  /**
   * Install all GIT related tasks.
   *
   * @param project
   * @param config
   */
  protected void appendGit(Project project, GradleConfig config) {
    project.task("git").doLast(t -> TaskExecutor.execGit(new GitTask(), config))
        .setDescription("Shows the git related informations!");
    project.task("git-reset").doLast(t -> TaskExecutor.execGit(new GitTaskCheckout(), config));
    project.task("git-reset-hard").doLast(t -> TaskExecutor.execGit(new GitTaskResetHard(), config));

    project.task("git-pull").doLast(t -> TaskExecutor.execGit(new GitTaskPull(), config));
    project.task("git-tag").doLast(t -> TaskExecutor.execGit(new GitTaskTag(), config));

    project.task("git-build").doLast(t -> TaskExecutor.execGit(new GitTaskVersion(), config));
    project.task("git-patch").doLast(t -> TaskExecutor.execGit(new GitTaskVersion(Git.Release.Patch), config));
    project.task("git-release").doLast(t -> TaskExecutor.execGit(new GitTaskVersion(Git.Release.Release), config));
  }

  /**
   * Install all pipeline related tasks. Parses the pipeline configuration
   *
   * @param project
   * @param config
   */
  @Deprecated
  protected void appendBuild(Project project, GradleConfig config) {
    project.task("build").doFirst(t -> {
      if (!config.hasParameter("task")) {
        project.getLogger().warn("Missing parameter 'task'!");
        return;
      }
      String names = config.getParameter("task");
      String name = names.contains(":") ? names.substring(0, names.indexOf(":")) : names;
      List<String> stages = names.contains(":") ? Arrays.asList(names.substring(names.indexOf(":") + 1).split(","))
          : Collections.emptyList();

      Pipeline pipeline = new Pipeline(config, project);
      pipeline.exec(name, stages.isEmpty() ? null : stages.get(0));
    }).setDescription("Executes a pipeline!");
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
          String taskName = String.format("%s-%s", p.name, s.name);
          project.task(taskName).doLast(t -> {
            Pipeline pipeline = new Pipeline(config, project);
            pipeline.exec(p.name, s.name);
          }).setDescription(s.title);
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
      Pipeline.newFactory().getTasks().forEach(n -> project.task("task-" + n.getName()).doLast(t -> {
        project.getLogger().warn(n.getName());
      }).setDescription(n.getDescription()));
    });
  }
}
