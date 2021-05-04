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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * The {@link EnvironmentVariables} provides additional environment variables.
 */
public class EnvironmentVariables implements Environment {

  private final Map<String, String> variables;
  private final Environment         environment;

  /**
   * Constructs an instance of {@link EnvironmentVariables}.
   *
   * @param variables
   * @param environment
   */
  public EnvironmentVariables(Map<String, String> variables, Environment environment) {
    this.variables = variables;
    this.environment = environment;
  }

  /**
   * Returns the names of all variables.
   */
  public final Set<String> getVariables() {
    return this.variables.keySet();
  }

  /**
   * Get the local variable by name.
   *
   * @param name
   */
  public final String getVariable(String name) {
    return this.variables.get(name);
  }

  /**
   * Set the local variable by name.
   *
   * @param name
   */
  public final void setVariable(String name, String value) {
    this.variables.put(name, value);
    this.variables.put(name, value);
  }

  /**
   * Returns true if the environment contains the named variable.
   *
   * @param name
   */
  @Override
  public final boolean isSet(String name) {
    return this.variables.containsKey(name) || this.environment.isSet(name);
  }

  /**
   * Returns the named environment variable, throws an exception otherwise.
   *
   * @param name
   */
  @Override
  public final String get(String name) {
    return this.variables.containsKey(name) ? this.variables.get(name) : this.environment.get(name);
  }

  /**
   * Converts the environment variables as {@link Map}.
   */
  @Override
  public final Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>(this.environment.toMap());
    map.putAll(this.variables);
    return map;
  }


  private Map<String, String> toVariables() {
    Map<String, String> vars =
        (this.environment instanceof EnvironmentVariables) ? ((EnvironmentVariables) this.environment).toVariables()
            : new HashMap<>();
    vars.putAll(this.variables);
    return this.variables;
  }

  /**
   * Creates a string of the local defined environment variables.
   */
  @Override
  public final String toString() {
    Map<String, String> vars = toVariables();
    return vars.isEmpty() ? ""
        : "\n" + vars.keySet().stream().sorted().map(k -> String.format("  %s\t= %s", k, vars.get(k)))
            .collect(Collectors.joining("\n"));
  }
}
