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

import java.util.Map;

/**
 * The {@link EnvironmentTree} class.
 */
class EnvironmentTree extends EnvironmentMap {

  private final Environment parent;

  /**
   * Constructs an instance of {@link EnvironmentTree}.
   *
   * @param map
   * @param parent
   */
  public EnvironmentTree(Map<String, String> map, Environment parent) {
    super(map);
    this.parent = parent;
  }

  /**
   * Gets the parent {@link Environment}.
   */
  protected final Environment getDelegate() {
    return this.parent;
  }

  /**
   * Return <code>true</code> if the parameter is set.
   *
   * @param name
   */
  @Override
  public final boolean isSet(String name) {
    return super.isSet(name) || getDelegate().isSet(name);
  }

  /**
   * Get a parameter by name.
   *
   * @param name
   */
  @Override
  public String get(String name) {
    return super.isSet(name) ? super.get(name) : getDelegate().get(name);
  }

  /**
   * Get the environment variables as map.
   */
  @Override
  public Map<String, String> toMap() {
    Map<String, String> variables = getDelegate().toMap();
    variables.putAll(super.toMap());
    return variables;
  }
}
