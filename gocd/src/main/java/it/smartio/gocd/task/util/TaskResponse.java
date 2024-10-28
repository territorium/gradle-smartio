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

package it.smartio.gocd.task.util;

import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class TaskResponse {

  private final boolean success;
  private final String  message;

  private TaskResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public int responseCode() {
    return this.success ? DefaultGoApiResponse.SUCCESS_RESPONSE_CODE : DefaultGoApiResponse.INTERNAL_ERROR;
  }

  public final GoPluginApiResponse toResponse() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("success", this.success);
    builder.add("message", this.message);
    return new DefaultGoPluginApiResponse(responseCode(), builder.build().toString());
  }

  public static TaskResponse success(String message) {
    return new TaskResponse(true, message);
  }

  public static TaskResponse failure(String message) {
    return new TaskResponse(false, message);
  }
}
