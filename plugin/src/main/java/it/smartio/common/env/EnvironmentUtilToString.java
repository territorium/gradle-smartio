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

package it.smartio.common.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.smartio.util.env.OS;

/**
 * The {@link EnvironmentUtilToString} class.
 */
abstract class EnvironmentUtilToString {

  private static final String PATTERN =
      String.format("{}%s {}={}\n", OS.isWindows() ? "set" : "export").replaceAll("\\{\\}", "%s");

  /**
   * Constructs an instance of {@link EnvironmentUtilToString}.
   */
  private EnvironmentUtilToString() {}

  /**
   * Creates the string for the {@link Environment}.
   *
   * @param environment
   */
  public static String toString(Environment environment) {
    StringBuffer buffer = new StringBuffer();
    String intent = "";
    List<Environment> list = new ArrayList<>();
    EnvironmentUtilToString.collect(environment, list);
    Map<String, String> variables = new HashMap<>();
    for (Environment e : list) {
      Map<String, String> values =
          e.toMap().entrySet().stream().filter(i -> !variables.containsKey(i.getKey()) && (i.getValue() != null))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      for (String name : values.keySet().stream().sorted().collect(Collectors.toList())) {
        buffer.append(String.format(EnvironmentUtilToString.PATTERN, intent, name, e.get(name)));
      }
      variables.putAll(values);
      intent += "  ";
    }
    return buffer.toString();
  }

  /**
   * Collects all {@link Environment}'s.
   *
   * @param list
   */
  private static void collect(Environment env, List<Environment> list) {
    if (env instanceof EnvironmentTree) {
      EnvironmentUtilToString.collect(((EnvironmentTree) env).getDelegate(), list);
    }
    list.add(env);
  }
}
