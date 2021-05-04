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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtensionContainer;

import it.smartio.build.QtPlatform;
import it.smartio.common.env.Environment;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskContextAsync;
import it.smartio.gradle.config.PipelineConfig;
import it.smartio.task.TaskBuilder;
import it.smartio.task.TaskFactory;
import it.smartio.task.env.EnvironmentTask;
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

  public static final String NAME_APP    = "smartIO";
  public static final String NAME_CONFIG = "smartIO";


  @Override
  public void apply(Project project) {
    ExtensionContainer extension = project.getExtensions();
    GradleConfig config = extension.create(BuildPlugin.NAME_CONFIG, GradleConfig.class);

    // Tasks
    project.task("env").doLast(t -> doExec(new EnvironmentTask(), config))
        .setDescription("Shows the current environment!");

    project.task("git").doLast(t -> new GitTask().handle(BuildPlugin.toGitContext(t, config)))
        .setDescription("Shows the git related informations!");
    project.task("git-reset").doLast(t -> new GitTaskCheckout().handle(BuildPlugin.toGitContext(t, config)));
    project.task("git-reset-hard").doLast(t -> new GitTaskResetHard().handle(BuildPlugin.toGitContext(t, config)));

    project.task("git-pull").doLast(t -> new GitTaskPull().handle(BuildPlugin.toGitContext(t, config)));
    project.task("git-tag").doLast(t -> new GitTaskTag().handle(BuildPlugin.toGitContext(t, config)));

    project.task("git-build").doLast(t -> new GitTaskVersion().handle(BuildPlugin.toGitContext(t, config)));
    project.task("git-patch")
        .doLast(t -> new GitTaskVersion(ProjectBuild.Patch).handle(BuildPlugin.toGitContext(t, config)));
    project.task("git-release")
        .doLast(t -> new GitTaskVersion(ProjectBuild.Release).handle(BuildPlugin.toGitContext(t, config)));

    project.task("unittest").doLast(t -> doExec(new XTestSuiteTask.XUnit(config.getParameter("test")), config));

    TaskFactory factory = GradleFactory.create();
    project.task("build").doFirst(t -> BuildPlugin.exec(config, t.getProject(), factory))
        .setDescription("Executes a pipeline!");

    // TODO
    project.task("branding").doLast(t -> {
      Arguments args = new Arguments(config.getProject().getProperties());
      doExec(new BrandingTask(args.get("source"), args.get("target")), config);
    }).setDescription("Creates the branding from a build.properties!");

    project.task("ipa-upload")
        .doLast(
            t -> doExec(new IPAUploadTask("smartIO", "app", (String) t.getProject().getProperties().get("artifact")),
                config))
        .setDescription("Executes a pipeline!");
  }

  /**
   * Executes a {@link it.smartio.common.task.Task}.
   *
   * @param task
   * @param config
   */
  public void doExec(it.smartio.common.task.Task task, GradleConfig config) {
    File workingDir = config.getWorkingDir();
    Environment environment = Environment.system();
    Logger logger = config.getProject().getLogger();

    logger.warn("WorkingDir: '{}'", workingDir);
    logger.warn("Loading environment variables...");
    try {
      environment = GradleEnvironment.parse(config, workingDir, environment);
    } catch (Throwable e) {
      logger.error("Couldn't load environment variables!", e);
      throw new RuntimeException(e);
    }
    logger.warn("Environment variables loaded!");

    GradleContext context = new GradleContext(logger, workingDir, environment);
    try {
      task.handle(context);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates the {@link TaskContext} for the current {@link Task} and {@link GitConfig}.
   *
   * @param task
   * @param config
   */
  private static TaskContext toGitContext(Task task, GradleConfig config) {
    Environment environment = Environment.of(config.toGitEnv());
    return new GradleContext(task.getProject().getLogger(), config.getWorkingDir(), environment);
  }

  /**
   * Executes a pipeline from the {@link GradleConfig}.
   *
   * @param config
   * @param project
   * @param factory
   */
  protected static void exec(GradleConfig config, Project project, TaskFactory factory) {
    Logger logger = project.getLogger();

    if (!config.hasParameter("task")) {
      logger.warn("Missing parameter 'task'!");
      return;
    }

    File workingDir = config.getWorkingDir();
    Environment environment = Environment.system();

    logger.warn("Environment variables: loading...");
    try {
      environment = GradleEnvironment.parse(config, workingDir, environment);
    } catch (IOException e) {
      logger.error("Couldn't load environment variables!", e);
      throw new RuntimeException(e);
    }
    logger.warn("Environment variables: loaded!");

    String names = config.getParameter("task");
    TaskBuilder builder = BuildPlugin.createPipeline(names, logger, config, factory, environment);
    if (builder == null) {
      logger.warn("Couldn't execute the pipeline '{}'!", names);
      return;
    }

    logger.warn("Pipeline '{}': starting...", names);
    try (TaskContextAsync context = new GradleContext(logger, workingDir, environment)) {
      builder.build().handle(context);
      logger.warn("Pipeline '{}': completed!", names);
    } catch (Throwable e) {
      logger.error("Pipeline '{}': terminated!", names, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Executes a pipeline from the {@link GradleConfig}.
   *
   * @param names
   * @param logger
   * @param config
   */
  protected static TaskBuilder createPipeline(String names, Logger logger, GradleConfig config, TaskFactory factory,
      Environment environment) {
    String name = names.contains(":") ? names.substring(0, names.indexOf(":")) : names;
    Optional<PipelineConfig> optional = config.getPipelines().stream().filter(c -> c.name.equals(name)).findFirst();
    if (!optional.isPresent()) {
      logger.warn("Pipeline '{}' not found!", name);
      return null;
    }

    File workingDir = config.getWorkingDir();
    Arguments arguments = new Arguments(config.getProject().getProperties());
    List<String> stages = names.contains(":") ? Arrays.asList(names.substring(names.indexOf(":") + 1).split(","))
        : Collections.emptyList();

    TaskBuilder builder = new TaskBuilder(name);
    optional.get().getStages().stream().filter(c -> stages.contains(c.name)).forEach(stage -> {
      TaskBuilder taskBuilder = builder.addTask(stage.name);

      stage.cmds.forEach(c -> taskBuilder.addTask(c, factory.createTask(c, arguments.merge(stage.args), workingDir)));
      stage.getTasks().stream().filter(t -> QtPlatform.isSupported(t.device, environment)).forEach(t -> taskBuilder
          .addTask(t.name, factory.createTask(t.name, arguments.merge(stage.args).merge(t.args), workingDir)));
    });
    return builder;
  }
}
