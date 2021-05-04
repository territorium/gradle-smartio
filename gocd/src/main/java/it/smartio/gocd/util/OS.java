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
