/*
 * Copyright 2024 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package it.smartio.gocd.task.gradle;

import java.io.File;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

import it.smartio.gocd.task.util.TaskRequest;
import it.smartio.gocd.task.util.TaskResponse;
import it.smartio.gocd.util.Environment;
import it.smartio.gocd.util.OS;
import it.smartio.gocd.util.request.RequestHandler;

/**
 * This message is sent by the GoCD agent to the plugin to execute the task.
 *
 * <pre>
 * {
 *   "config": {
 *     "ftp_server": {
 *       "secure": false,
 *       "value": "ftp.example.com",
 *       "required": true
 *     },
 *     "remote_dir": {
 *       "secure": false,
 *       "value": "/pub/",
 *       "required": true
 *     }
 *   },
 *   "context": {
 *     "workingDirectory": "working-dir",
 *     "environmentVariables": {
 *       "ENV1": "VAL1",
 *       "ENV2": "VAL2"
 *     }
 *   }
 * }
 * </pre>
 */
public class GradleTask implements RequestHandler {

  private static final String    GO_PIPELINE_ROOT = "GO_PIPELINE_ROOT";

  private final JobConsoleLogger console;

  /**
   * Constructs an instance of {@link GradleTask}.
   *
   * @param console
   */
  public GradleTask(JobConsoleLogger console) {
    this.console = console;
  }

  /**
   * Handles a request and provides a response.
   *
   * @param request
   */
  @Override
  public GoPluginApiResponse handle(GoPluginApiRequest request) {
    TaskRequest task = TaskRequest.parse(request);
    String command = task.getValue(GradleConfig.COMMAND);
    String parameter = task.getValue(GradleConfig.PARAMETER);
    String directory = task.getValue(GradleConfig.WORKINGDIR);

    File workingDir = new File(task.getWorkingDirectory());
    Environment environment = OS.environment(task.getEnvironment().toMap());
    environment.set(GradleTask.GO_PIPELINE_ROOT, workingDir.getAbsolutePath());

    GradleBuilder gradle = GradleBuilder.create(command);
    gradle.parseArguments(parameter);

    ProcessBuilder builder = new ProcessBuilder();
    builder.environment().putAll(environment.toMap());
    builder.directory((directory == null) ? workingDir : new File(workingDir, directory));
    builder.command(gradle.build());

    this.console.printLine("GoCD Working Directory: " + builder.directory());
    this.console.printLine("GoCD Launching command: " + String.join(" ", builder.command()));

    try {
      Process process = builder.start();

      try {
        this.console.readErrorOf(process.getErrorStream());
        this.console.readOutputOf(process.getInputStream());

        int exitCode = process.waitFor();
        if (exitCode != 0) {
          return TaskResponse.failure("Could not execute build! Process returned with status code " + exitCode)
              .toResponse();
        }

        return TaskResponse.success("Executed the build").toResponse();
      } finally {
        process.destroy();
      }
    } catch (Throwable e) {
      this.console.printEnvironment(builder.environment());
      this.console.printLine("" + e);
      return TaskResponse.failure(e.getMessage()).toResponse();
    }
  }
}
