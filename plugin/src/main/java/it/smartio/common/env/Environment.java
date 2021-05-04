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

import java.util.Collections;
import java.util.Map;

/**
 * The {@link Environment} provides the environment variables for an execution context. Depends of
 * the {@link Environment} implementation if the variables are available in read-only mode or in
 * write mode.
 */
public interface Environment {

  /**
   * Return <code>true</code> if the parameter is set.
   *
   * @param name
   */
  boolean isSet(String name);

  /**
   * Get a parameter by name.
   *
   * @param name
   */
  String get(String name);

  /**
   * Get the environment variables as map.
   */
  Map<String, String> toMap();

  /**
   * Creates a new {@link Environment} adding the {@link Map} for fallback's.
   *
   * @param map
   */
  default Environment map(Map<String, String> map) {
    return new EnvironmentTree(map, this);
  }

  /**
   * Creates an empty {@link Environment}.
   */
  static Environment empty() {
    return Environment.of(Collections.emptyMap());
  }

  /**
   * Creates an {@link Environment} from the system environment.
   */
  static Environment system() {
    return Environment.of(System.getenv());
  }

  /**
   * Creates an {@link Environment} for the map.
   *
   * @param map
   */
  static Environment of(Map<String, String> map) {
    return new EnvironmentMap(map);
  }
}
