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

package it.smartio.gocd.task.util;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Get the request for a task config.
 *
 * <pre>
 * {
 *   "URL": {
 *     "secure": false,
 *     "value": "http://localhost.com",
 *     "required": true
 *   },
 *   "USERNAME": {
 *     "secure": false,
 *     "value": "user",
 *     "required": false
 *   },
 *   "PASSWORD": {
 *     "secure": true,
 *     "value": "password",
 *     "required": false
 *   }
 * }
 * </pre>
 */
public class TaskConfig {

  private final Map<String, Item> config = new HashMap<>();


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
   * Parses the configuration from the text.
   *
   * @param text
   */
  public final void parse(String text) {
    parse(Json.createReader(new StringReader(text)).readObject());
  }

  /**
   * Parses the configuration from the text.
   *
   * @param text
   */
  public final void parse(JsonObject json) {
    for (String name : json.keySet()) {
      JsonObject config = json.getJsonObject(name);
      String value = config.containsKey("value") ? config.getString("value") : "";
      boolean secure = config.getBoolean("secure");
      boolean required = config.getBoolean("required");
      this.config.put(name, new Item(value, secure, required));
    }
  }

  /**
   * Parses the {@link TaskConfig} from the {@link GoPluginApiRequest}.
   *
   * @param request
   */
  public static TaskConfig of(GoPluginApiRequest request) {
    TaskConfig taskRequest = new TaskConfig();
    taskRequest.parse(request.requestBody());
    return taskRequest;
  }

  /**
   * The {@link Item} class.
   */
  private class Item {

    private final String  value;
    private final boolean secure;
    private final boolean required;

    /**
     * Constructs an instance of {@link Item}.
     *
     * @param value
     * @param secure
     * @param required
     */
    private Item(String value, boolean secure, boolean required) {
      this.value = value;
      this.secure = secure;
      this.required = required;
    }
  }
}