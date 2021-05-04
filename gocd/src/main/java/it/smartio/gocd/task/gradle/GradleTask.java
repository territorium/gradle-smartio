/*
 * Copyright 2017 ThoughtWorks, Inc.
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

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  private static final String  GO_PIPELINE_ROOT  = "GO_PIPELINE_ROOT";
  private static final Pattern GRADLE_PARAMETERS = Pattern.compile("([^=\n]+)=([^\n]+)");


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
    TaskRequest task = TaskRequest.of(request);
    String command = task.getConfig().getValue(GradleConfig.COMMAND);
    String parameter = task.getConfig().getValue(GradleConfig.PARAMETER);
    String directory = task.getConfig().getValue(GradleConfig.WORKINGDIR);

    File workingDir = new File(task.getWorkingDirectory());
    Environment environment = OS.environment(task.getEnvironment().toMap());
    environment.set(GradleTask.GO_PIPELINE_ROOT, workingDir.getAbsolutePath());

    List<String> arguments = new ArrayList<>();
    arguments.add(OS.isWindows() ? "gradlew.bat" : "./gradlew");
    arguments.add(command);

    if (parameter != null) {
      Matcher matcher = GradleTask.GRADLE_PARAMETERS.matcher(parameter.replace("\\\n", ""));
      while (matcher.find()) {
        arguments.add(GradleTask.getGradleParameter(matcher.group(1), matcher.group(2)));
      }
    }

    List<String> commands = new ArrayList<>();
    commands.add(OS.isWindows() ? "cmd" : "sh");
    commands.add(OS.isWindows() ? "/c" : "-c");
    commands.add(String.join(" ", arguments));

    ProcessBuilder builder = new ProcessBuilder();
    builder.environment().putAll(environment.toMap());
    builder.directory((directory == null) ? workingDir : new File(workingDir, directory));
    builder.command(commands);

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


  /**
   * Formats a gradle parameter for the specific platform.
   *
   * @param name
   * @param value
   */
  private static final String getGradleParameter(String name, String value) {
    String pattern = OS.isWindows() ? "-P%s=%s" : "-P%s='%s'";
    return String.format(pattern, name, value);
  }
}
