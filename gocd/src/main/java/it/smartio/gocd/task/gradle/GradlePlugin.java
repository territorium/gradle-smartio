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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

import java.util.Arrays;

import it.smartio.gocd.util.request.Request;
import it.smartio.gocd.util.request.ViewHandler;

/**
 * GoPlugin interface represents Go plugin. It is necessary to implement this interface for any
 * plugin implementation to be recognized as a Go plugin
 */
@Extension
public class GradlePlugin implements GoPlugin {

  private static final Logger LOGGER = Logger.getLoggerFor(GradlePlugin.class);


  /**
   * Provides an instance of GoPluginIdentifier, providing details about supported extension point
   * and its versions
   */
  @Override
  public GoPluginIdentifier pluginIdentifier() {
    return new GoPluginIdentifier("task", Arrays.asList("1.0"));
  }

  /**
   * Initializes an instance of GoApplicationAccessor. This method would be invoked before Go
   * interacts with plugin to handle any GoPluginApiRequest. Instance of GoApplicationAccessor will
   * allow plugin to communicate with Go.
   *
   * @param accessor
   */
  @Override
  public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {}

  /**
   * Handles GoPluginApiRequest request submitted from Go to plugin implementation and returns
   * result as GoPluginApiResponse
   *
   * @param request
   */
  @Override
  public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
    try {
      switch (request.requestName()) {
        case Request.TASK_VIEW:
          return new ViewHandler("Gradle", "/task.template.html").handle(request);

        case Request.TASK_CONFIG:
          return GradleConfig.createGoApiResponse();

        case Request.TASK_VALIDATE:
          return new GradleValidator().handle(request);

        case Request.TASK_EXECUTE:
          return new GradleTask(JobConsoleLogger.getConsoleLogger()).handle(request);

        default:
          throw new UnhandledRequestTypeException(request.requestName());
      }
    } catch (Exception e) {
      GradlePlugin.LOGGER.error("Error while executing request " + request.requestName(), e);
      throw new RuntimeException(e);
    }
  }
}
