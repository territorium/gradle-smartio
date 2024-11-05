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

package it.smartio.gocd.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link Environment} provides a mapping to environment variables. The {@link Environment}
 * allows to update text parts, containing parameters, with the environment variables.
 */
public class Environment {

  private static final Pattern NAMES  = Pattern.compile("\\(\\?<([a-z][a-z_0-9]*)>", Pattern.CASE_INSENSITIVE);
  private static final Pattern PARAMS = Pattern.compile("\\$([0-9]+|[a-z][a-z_0-9]*)", Pattern.CASE_INSENSITIVE);


  private final Map<String, String> environment;

  /**
   * Constructs an instance of {@link Environment}.
   */
  public Environment() {
    this(new HashMap<>());
  }

  /**
   * Constructs an instance of {@link Environment}.
   *
   * @param environment
   */
  private Environment(Map<String, String> environment) {
    this.environment = environment;
  }

  /**
   * Set a new parameter to the {@link Environment}.
   */
  public final Map<String, String> toMap() {
    return this.environment;
  }

  /**
   * <code>true</code> if the parameter is set.
   *
   * @param name
   */
  public final boolean isSet(String name) {
    return this.environment.containsKey(name);
  }

  /**
   * Get a parameter by name.
   *
   * @param name
   */
  public final String get(String name) {
    return this.environment.get(name);
  }

  /**
   * Set a new parameter to the {@link Environment}.
   *
   * @param name
   * @param value
   */
  public final Environment set(String name, String value) {
    this.environment.put(name, value);
    return this;
  }

  /**
   * Replaces the indexed or named placeholder's with the the parameter values.
   *
   * @param pattern
   */
  public final String replaceByPattern(String pattern) {
    StringBuffer buffer = new StringBuffer();
    int offset = 0;

    Matcher matcher = Environment.PARAMS.matcher(pattern);
    while (matcher.find()) {
      String name = matcher.group(1);
      String value = this.environment.get(name);
      buffer.append(pattern.substring(offset, matcher.start(1) - 1));
      if (value == null) {
        buffer.append("$" + name);
      } else {
        buffer.append(value);
      }
      offset = matcher.end(1);
    }
    buffer.append(pattern.substring(offset, pattern.length()));
    return buffer.toString();
  }

  /**
   * Parses the group names from the pattern.
   *
   * @param pattern
   */
  public static Set<String> getGroupNames(String pattern) {
    Set<String> names = new HashSet<>();
    Matcher matcher = Environment.NAMES.matcher(pattern);
    while (matcher.find()) {
      names.add(matcher.group(1));
    }
    return names;
  }

  /**
   * Get the indexed parameters from the matcher.
   *
   * @param matcher
   * @param names
   */
  public static Map<String, String> getParameters(Matcher matcher, Set<String> names) {
    Map<String, String> params = new HashMap<>();
    params.put(Integer.toString(0), matcher.group(0));
    for (int index = 0; index < matcher.groupCount(); index++) {
      params.put(Integer.toString(index + 1), matcher.group(index + 1));
    }
    for (String name : names) {
      params.put(name, matcher.group(name));
    }
    return params;
  }

  /**
   * Load additional environment variables.
   * 
   * @param file
   */
  public final Environment load(File file) throws FileNotFoundException, IOException {
    if (file.exists()) {
      try (FileReader reader = new FileReader(file)) {
        Properties properties = new Properties();
        properties.load(reader);
        properties.forEach((n, v) -> this.environment.put("" + n, "" + v));
      }
    }
    return this;
  }

  /**
   * Constructs an instance of {@link Environment}.
   */
  @Override
  public final Environment clone() {
    return Environment.of(this.environment);
  }

  /**
   * Constructs an instance of {@link Environment}.
   *
   * @param environment
   */
  public static Environment of(Map<String, String> environment) {
    return new Environment(new HashMap<>(environment));
  }
}