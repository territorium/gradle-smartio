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

import java.util.Map;

/**
 * The {@link OS} defines the available operating systems.
 */
public enum OS {

  MACOS,
  LINUX,
  WINDOWS;

  private static OS instance = null;

  /**
   * Return <code>true</code> if it is windows.
   */
  public static boolean isMacOS() {
    return OS.current() == OS.MACOS;
  }

  /**
   * Return <code>true</code> if it is windows.
   */
  public static boolean isLinux() {
    return OS.current() == OS.LINUX;
  }

  /**
   * Return <code>true</code> if it is windows.
   */
  public static boolean isWindows() {
    return OS.current() == OS.WINDOWS;
  }

  /**
   * Get the current operating system.
   */
  public static OS current() {
    if (OS.instance == null) {
      String name = System.getProperty("os.name").toLowerCase();
      if (name.contains("windows")) {
        OS.instance = OS.WINDOWS;
      } else if (name.contains("mac")) {
        OS.instance = OS.MACOS;
      } else {
        OS.instance = OS.LINUX;
      }
    }
    return OS.instance;
  }

  /**
   * Constructs an instance of {@link Environment}.
   *
   * @param variables
   */
  public static Environment environment(Map<String, String> variables) {
    String architecture = String.format("[%s]", OS.current().name());
    Environment environment = new Environment();
    for (String variable : variables.keySet()) {
      if (!variable.startsWith("[")) {
        environment.set(variable, variables.get(variable));
      } else if (variable.startsWith(architecture)) {
        environment.set(variable.substring(variable.indexOf(']') + 1), variables.get(variable));
      }
    }
    return environment;
  }
}
