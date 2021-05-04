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

package it.smartio.common.task.process;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import it.smartio.build.Build;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;

/**
 * The {@link ProcessTask} class.
 */
public abstract class ProcessTask implements Task {

  private final boolean throwIfFailed;

  /**
   * Constructs an instance of {@link ProcessTask}.
   */
  protected ProcessTask() {
    this(true);
  }

  /**
   * Constructs an instance of {@link ProcessTask}.
   *
   * @param throwIfFailed
   */
  protected ProcessTask(boolean throwIfFailed) {
    this.throwIfFailed = throwIfFailed;
  }

  /**
   * Get the {@link ProcessRequest}.
   */
  protected abstract ProcessRequestBuilder getShellBuilder(TaskContext context);

  /**
   * Update the Path environment variables.
   *
   * @param environment
   * @param shell
   */
  private void updateEnvironmentVariables(Map<String, String> environment, ProcessRequest shell) {
    if (shell.getEnvironment().isSet(Build.PATH_WIN64)) {
      String path = shell.getEnvironment().get(Build.PATH_WIN64);
      if (environment.containsKey(Build.PATH_WIN64)) {
        path += ";" + environment.get(Build.PATH_WIN64);
      }
      environment.put(Build.PATH_WIN64, path);
    }

    if (shell.getEnvironment().isSet(Build.LD_LIBRARY_PATH)) {
      String path = shell.getEnvironment().get(Build.LD_LIBRARY_PATH);
      if (environment.containsKey(Build.LD_LIBRARY_PATH)) {
        path += ":" + environment.get(Build.LD_LIBRARY_PATH);
      }
      environment.put(Build.LD_LIBRARY_PATH, path);
    }

    if (shell.getEnvironment().isSet(Build.QT_ARCH)) {
      environment.put(Build.QT_ARCH, shell.getEnvironment().get(Build.QT_ARCH));
    }
    if (shell.getEnvironment().isSet(Build.QT_BUILD)) {
      environment.put(Build.QT_BUILD, shell.getEnvironment().get(Build.QT_BUILD));
    }
  }

  /**
   * Print the environment to the {@link TaskContext}
   *
   * @param process
   */
  private String printEnvironment(ProcessBuilder process) {
    Map<String, String> e = process.environment();
    StringBuilder builder = new StringBuilder();
    builder.append(e.keySet().stream().sorted().map(k -> String.format("export %s=%s", k, e.get(k)))
        .collect(Collectors.joining("\n")));
    builder.append("\n");
    builder.append(String.format("WorkingDir: %s\n", process.directory()));
    builder.append(String.format("Command Line: %s\n", String.join(" ", process.command())));
    return builder.toString();
  }

  /**
   * Executes the {@link TaskContext} in a different process.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) throws IOException {
    ProcessRequest shell = getShellBuilder(context).build();

    ProcessBuilder builder = new ProcessBuilder(shell.getCommand());
    builder.directory(shell.getWorkingDir());
    builder.environment().putAll(context.getEnvironment().toMap());
    updateEnvironmentVariables(builder.environment(), shell);

    context.getLogger().onInfo(printEnvironment(builder));

    Process process = null;
    try {
      process = builder.start();
      context.getLogger().onRedirect(process.getInputStream(), process.getErrorStream());

      if ((process.waitFor() != 0) && this.throwIfFailed) {
        throw new RuntimeException("Process finished with an error!");
      }
    } catch (InterruptedException e) {
      throw new IOException(e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }
}
