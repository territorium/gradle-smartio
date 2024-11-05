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

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.options.OptionValues;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.smartio.common.env.Environment;
import it.smartio.gradle.BuildPlugin;
import it.smartio.gradle.GradleConfig;
import it.smartio.gradle.GradleContext;
import it.smartio.task.git.Git;
import it.smartio.task.git.GitTask;
import it.smartio.task.git.GitTaskCheckout;
import it.smartio.task.git.GitTaskPull;
import it.smartio.task.git.GitTaskResetHard;
import it.smartio.task.git.GitTaskTag;
import it.smartio.task.git.GitTaskVersion;


/**
 * The {@link TaskGit} class.
 */
public abstract class TaskGit extends DefaultTask {

  public TaskGit() {
    setGroup("smart.io");
    setDescription("Provides GIT commands on the gradle shell!");
  }

  @Input
  @Optional
  @Option(option = "workingDir", description = "Defines an alternative GIT working directory")
  public abstract Property<String> getWorkingDir();

  @Input
  @Optional
  @Option(option = "remote", description = "Defines the remote endpoint")
  public abstract Property<String> getRemote();

  @Input
  @Optional
  @Option(option = "username", description = "Defines the username")
  public abstract Property<String> getUsername();

  @Input
  @Optional
  @Option(option = "password", description = "Defines the password")
  public abstract Property<String> getPassword();

  @Input
  @Optional
  @Option(option = "modules", description = "Defines the sub-modules")
  public abstract Property<String> getModules();

  @Input
  @Optional
  @Option(option = "branch", description = "Defines the branch")
  public abstract Property<String> getBranch();

  @Input
  @Optional
  @Option(option = "pull", description = "Forces a pull if set")
  public abstract Property<Boolean> getPull();

  @Input
  @Optional
  @Option(option = "option", description = "Get the git option.")
  public abstract Property<OptionType> getOption();

  @OptionValues("option")
  public List<OptionType> getAvailableOutputTypes() {
    return new ArrayList<OptionType>(Arrays.asList(OptionType.values()));
  }

  @TaskAction
  public void process() {
    GradleConfig config = BuildPlugin.findConfig(getProject());
    Project project = config.getProject();
    File workingDir = getWorkingDir().isPresent() ? new File(getWorkingDir().get()) : project.getProjectDir();

    if (!workingDir.isAbsolute()) {
      workingDir = project.getProjectDir().toPath().resolve(workingDir.toPath()).toFile();
    }

    Map<String, String> env = new HashMap<>();
    if (config.remote != null) {
      env.put(Git.REMOTE, config.remote);
    }
    env.put(Git.BRANCH, getBranch().isPresent() ? getBranch().get() : config.branch);
    env.put(Git.USERNAME, getUsername().isPresent() ? getUsername().get() : config.username);
    env.put(Git.PASSWORD, getPassword().isPresent() ? getPassword().get() : config.password);
    if (getModules().isPresent()) {
      env.put(Git.MODULES, getModules().get());
    }

    Environment environment = Environment.of(env);
    GradleContext context = new GradleContext(project.getLogger(), workingDir, environment);

    createTask().handle(context);
  }

  protected GitTask createTask() {
    if (getOption().isPresent()) {
      switch (getOption().get()) {
        case RESET:
          return new GitTaskCheckout();
        case HARD:
          return new GitTaskResetHard();
        case TAG:
          return new GitTaskTag();
        case BUILD:
          return new GitTaskVersion();
        case PATCH:
          return new GitTaskVersion(Git.Release.Patch);
        case RELEASE:
          return new GitTaskVersion(Git.Release.Release);
      }
    }
    return getPull().getOrElse(true) ? new GitTaskPull() : new GitTask();
  }


  private static enum OptionType {
    RESET,
    HARD,
    TAG,
    BUILD,
    PATCH,
    RELEASE
  }
}
