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

package it.smartio.gradle.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Nested;

/**
 * The {@link StageConfig} class.
 */
public class StageConfig {

  private final Project project;


  public String         name;
  public List<String>   device;

  public List<String>   cmds;
  public Map<String, ?> args = new HashMap<>();


  private final ListProperty<TaskConfig> tasks;

  @Inject
  public StageConfig(Project project) {
    this.project = project;
    this.device = new ArrayList<>();
    this.cmds = new ArrayList<>();
    this.tasks = project.getObjects().listProperty(TaskConfig.class).empty();
  }

  @Nested
  public final List<TaskConfig> getTasks() {
    return this.tasks.get();
  }

  public final void task(Action<? super TaskConfig> action) {
    TaskConfig item = this.project.getObjects().newInstance(TaskConfig.class);
    action.execute(item);
    this.tasks.add(item);
  }
}
