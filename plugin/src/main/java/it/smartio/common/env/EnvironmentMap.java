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

/**
 * The {@link EnvironmentMap} provides a simple map of environment variables. The is read-only, it
 * is not possible to change the values of the environment variable.
 */
class EnvironmentMap implements Environment {

  private final Map<String, String> map;

  /**
   * Constructs an instance of {@link EnvironmentMap}.
   *
   * @param map
   */
  public EnvironmentMap(Map<String, String> map) {
    this.map = map;
  }

  /**
   * <code>true</code> if the parameter is set.
   *
   * @param name
   */
  @Override
  public boolean isSet(String name) {
    return this.map.containsKey(name);
  }

  /**
   * Get a parameter by name.
   *
   * @param name
   */
  @Override
  public String get(String name) {
    return this.map.get(name);
  }

  /**
   * Get the environment variables as map.
   */
  @Override
  public Map<String, String> toMap() {
    return new HashMap<>(this.map);
  }

  /**
   * Constructs an instance of {@link Environment}.
   */
  @Override
  public final String toString() {
    return EnvironmentUtilToString.toString(this);
  }
}
