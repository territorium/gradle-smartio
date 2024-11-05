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

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import it.smartio.gocd.task.util.ConfigResponse;

/**
 * The {@link GradleConfig} class.
 */
public interface GradleConfig {

  String COMMAND    = "command";
  String PARAMETER  = "parameter";
  String WORKINGDIR = "workingDir";

  /**
   * Create a {@link ConfigResponse}.
   */
  public static ConfigResponse create() {
    ConfigResponse config = new ConfigResponse();
    config.setValue(GradleConfig.COMMAND, null, "Command", "1", true, false);
    config.setValue(GradleConfig.PARAMETER, null, "Parameters", "2", true, false);
    config.setValue(GradleConfig.WORKINGDIR, null, "Gradle Directory", "3", true, false);
    return config;
  }

  /**
   * Handles a request and provides a response.
   *
   * <pre>
   * {
   *   "url": {
   *     "default-value": "",
   *     "secure": false,
   *     "required": true
   *   },
   *   "user": {
   *     "default-value": "bob",
   *     "secure": true,
   *     "required": true
   *   },
   *   "password": {}
   * }
   * </pre>
   *
   * @param request
   */
  public static GoPluginApiResponse createGoApiResponse() {
    return GradleConfig.create().build();
  }
}
