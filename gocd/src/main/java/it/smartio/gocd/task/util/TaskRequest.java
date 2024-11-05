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

package it.smartio.gocd.task.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

import it.smartio.gocd.util.Environment;

/**
 * Get the request for a task execution.
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
public class TaskRequest {

  public final Map<String, Item> config      = new HashMap<>();
  private String                 workingDirectory;
  private final Environment      environment = new Environment();

  /**
   * Gets the working directory.
   */
  public final String getWorkingDirectory() {
    return this.workingDirectory;
  }

  /**
   * Gets the environment variables
   */
  public final Environment getEnvironment() {
    return this.environment;
  }

  /**
   * Get all names of the configuration
   */
  public final Set<String> getNames() {
    return this.config.keySet();
  }

  /**
   * Gets the configuration value
   */
  public final String getValue(String name) {
    return this.config.containsKey(name) ? this.config.get(name).value : null;
  }

  /**
   * Return <code>true</code> if the property should be secured.
   */
  public final boolean isSecure(String name) {
    return this.config.containsKey(name) ? this.config.get(name).secure : false;
  }

  /**
   * Return <code>true</code> if the property is required.
   */
  public final boolean isRequired(String name) {
    return this.config.containsKey(name) ? this.config.get(name).required : false;
  }


  /**
   * The {@link Item} class.
   */
  private static class Item {

    private final String  value;
    private final boolean secure;
    private final boolean required;

    private Item(String value, boolean secure, boolean required) {
      this.value = value;
      this.secure = secure;
      this.required = required;
    }
  }

  public static TaskRequest parse(GoPluginApiRequest request) {
    String text = request.requestBody();
    JsonObject root = Json.createReader(new StringReader(text)).readObject();
    JsonObject json = root.getJsonObject("config");
    JsonObject context = root.getJsonObject("context");
    JsonObject environment = context.getJsonObject("environmentVariables");

    TaskRequest taskRequest = new TaskRequest();

    for (String name : json.keySet()) {
      JsonObject config = json.getJsonObject(name);
      String value = config.containsKey("value") ? config.getString("value") : "";
      boolean secure = config.getBoolean("secure");
      boolean required = config.getBoolean("required");
      taskRequest.config.put(name, new Item(value, secure, required));
    }
    taskRequest.workingDirectory = context.getString("workingDirectory");
    for (String name : environment.keySet()) {
      taskRequest.environment.set(name, environment.getString(name));
    }
    return taskRequest;
  }
}