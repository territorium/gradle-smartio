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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Nested;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import it.smartio.common.env.Environment;
import it.smartio.gradle.config.AndroidConfig;
import it.smartio.gradle.config.MailConfig;
import it.smartio.gradle.config.PipelineConfig;
import it.smartio.gradle.config.XCodeConfig;
import it.smartio.task.git.Git;

/**
 * The {@link GradleConfig} class.
 */
public abstract class GradleConfig {

  private final Project project;


  public String remote;
  public String username;
  public String password;
  public String branch;


  public List<String> developer;

  public String       buildDir;
  public String       targetDir;

  public String       qtRoot;
  public String       qtVersion;
  public List<String> qtConfig;
  public String       qtAndroid;

  public String       msvcRoot;
  public String       msvcVersion;

  public String       androidSdkRoot;
  public String       androidNdkVersion;
  public String       androidNdkPlatform;
  public List<String> androidAbis;


  private final ListProperty<PipelineConfig> pipelines;

  /**
   * Constructs an instance of {@link GradleConfig}.
   *
   * @param project
   */
  @Inject
  public GradleConfig(Project project) {
    this.project = project;
    this.pipelines = project.getObjects().listProperty(PipelineConfig.class).empty();
  }

  /**
   * Gets the {@link Project}.
   */
  public final Project getProject() {
    return this.project;
  }

  /**
   * Gets the working directory.
   */
  public final File getWorkingDir() {
    if (!hasParameter("workingDir")) {
      return this.project.getProjectDir();
    }
    File workingDir = new File(getParameter("workingDir"));
    if (workingDir.isAbsolute()) {
      return workingDir;
    }
    return getProject().getProjectDir().toPath().resolve(workingDir.toPath()).toFile();
  }

  /**
   * Return <code>true</code> if the parameter is defined.
   *
   * @param name
   */
  @Deprecated
  public final boolean hasParameter(String name) {
    return this.project.getProperties().containsKey(name);
  }

  /**
   * Return the parameter or return <code>null</code> otherwise.
   *
   * @param name
   */
  @Deprecated
  public final String getParameter(String name) {
    return (String) this.project.getProperties().get(name);
  }

  @Nested
  public abstract MailConfig getEmail();

  public final void email(Action<? super MailConfig> action) {
    action.execute(getEmail());
  }

  @Nested
  public abstract XCodeConfig getIos();

  public final void ios(Action<? super XCodeConfig> action) {
    action.execute(getIos());
  }

  @Nested
  public abstract AndroidConfig getAndroid();

  public final void android(Action<? super AndroidConfig> action) {
    action.execute(getAndroid());
  }

  @Nested
  public final List<PipelineConfig> getPipelines() {
    return this.pipelines.get();
  }

  public final void pipeline(Action<? super PipelineConfig> action) {
    this.pipelines.add(newInstance(PipelineConfig.class, action));
  }

  private <C> C newInstance(Class<C> clazz, Action<? super C> action) {
    C instance = this.project.getObjects().newInstance(clazz);
    action.execute(instance);
    return instance;
  }

  /**
   * Converts the {@link GitConfig} to an {@link Environment} instance.
   */
  public final Map<String, String> toGitEnv() {
    Map<String, String> env = new HashMap<>();
    if (this.remote != null) {
      env.put(Git.REMOTE, this.remote);
    }
    env.put(Git.BRANCH, hasParameter("branch") ? getParameter("branch") : this.branch);
    env.put(Git.USERNAME, hasParameter("username") ? getParameter("username") : this.username);
    env.put(Git.PASSWORD, hasParameter("password") ? getParameter("password") : this.password);
    if (hasParameter("modules")) {
      env.put(Git.MODULES, getParameter("modules"));
    }
    return env;
  }

  /**
   * Gets the {@link Environment}.
   * 
   * @param logger
   * @param workingDir
   */
  public final Environment getEnvironment(Logger logger, File workingDir) {
    Environment environment = Environment.system();
    logger.warn("Environment variables: loading...");
    try {
      environment = GradleEnvironment.parse(this, workingDir, environment);
    } catch (IOException e) {
      logger.error("Couldn't load environment variables!", e);
      throw new RuntimeException(e);
    }
    logger.warn("Environment variables: loaded!");
    return environment;
  }
}
