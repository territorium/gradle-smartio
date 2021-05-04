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

package it.smartio.util.http.postman;

import java.io.Reader;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import it.smartio.util.test.TestResult;

/**
 * The {@link JsonUtil} class.
 */
public class JsonUtil {

  /**
   * Get the body as {@link JsonArray}.
   *
   * @param content
   */
  public static JsonArray getJsonArray(String content) {
    Reader reader = new StringReader(content);
    return Json.createReader(reader).readArray();
  }

  /**
   * Get the body as {@link JsonObject}.
   *
   * @param content
   */
  public static JsonObject getJsonObject(String content) {
    Reader reader = new StringReader(content);
    return Json.createReader(reader).readObject();
  }

  /**
   * Asserts the json data.
   *
   * @param result
   * @param expected
   * @param actual
   */
  public static void assertJson(TestResult result, String expected, String actual) {
    JsonObject jsonExpected = Json.createReader(new StringReader(expected)).readObject();
    JsonObject jsonResponse = Json.createReader(new StringReader(actual)).readObject();
    JsonUtil.assertJson(result, jsonExpected, jsonResponse);

  }

  /**
   * Do assert the actual value against the expected.
   *
   * @param expected
   * @param actual
   */
  public static void assertJson(TestResult result, JsonObject expected, JsonObject actual) {
    for (String name : expected.keySet()) {
      if (!actual.containsKey(name)) {
        result.addFailure(expected.get(name).getValueType(), null, "Json key '%s'", name);
      }
      result.assertEnum(expected.get(name).getValueType(), actual.get(name).getValueType(), "Json key '%s'", name);

      switch (expected.get(name).getValueType()) {
        case NUMBER:
          result.assertNumber(expected.getJsonNumber(name).doubleValue(), actual.getJsonNumber(name).doubleValue(),
              "Json key '%s'", name);
          break;
        case STRING:
          result.assertString(expected.getString(name), actual.getString(name), "Json key '%s'", name);
          break;
        case ARRAY:
          JsonUtil.assertJson(result, expected.getJsonArray(name), actual.getJsonArray(name));
          break;
        case OBJECT:
          JsonUtil.assertJson(result, expected.getJsonObject(name), actual.getJsonObject(name));
          break;
        default:
          break;
      }
    }
  }

  /**
   * Assert a {@link JsonObject} against the expected json.
   *
   * @param expected
   * @param actual
   */
  public static void assertJson(TestResult request, JsonArray expected, JsonArray actual) {
    request.assertInt(expected.size(), actual.size(), "Different size of arrays");
    for (int index = 0; index < expected.size(); index++) {
      if (request.assertEnum(expected.get(index).getValueType(), actual.get(index).getValueType(), "Json key '%s'",
          index)) {
        switch (expected.get(index).getValueType()) {
          case NUMBER:
            request.assertNumber(expected.getJsonNumber(index).doubleValue(), actual.getJsonNumber(index).doubleValue(),
                "Json key '%s'", index);
            break;
          case STRING:
            request.assertString(expected.getString(index), actual.getString(index), "Json key '%s'", index);
            break;
          case ARRAY:
            JsonUtil.assertJson(request, expected.getJsonArray(index), actual.getJsonArray(index));
            break;
          case OBJECT:
            JsonUtil.assertJson(request, expected.getJsonObject(index), actual.getJsonObject(index));
            break;
          default:
            break;
        }
      }
    }
  }
}
