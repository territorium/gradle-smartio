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

package it.smartio.gradle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link Arguments} class.
 */
public class Arguments {

  private final Map<String, ?> arguments;

  /**
   * Constructs an instance of {@link Arguments}.
   *
   * @param arguments
   */
  public Arguments(Map<String, ?> arguments) {
    this.arguments = arguments;
  }

  /**
   * Return <code>true</code> if there are no arguments.
   */
  public final boolean contains(String... names) {
    for (String name : names) {
      if (!this.arguments.containsKey(name)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get the typed gradle argument.
   *
   * @param name
   */
  @SuppressWarnings("unchecked")
  public final <T> T get(String name) {
    return (T) this.arguments.get(name);
  }

  /**
   * Get the typed gradle argument.
   *
   * @param name
   * @param value
   */
  public final <T> T get(String name, T value) {
    return this.arguments.containsKey(name) ? get(name) : value;
  }

  /**
   * Get the typed gradle argument.
   *
   * @param name
   */
  public final <T> List<T> asList(String name) {
    return get(name);
  }

  /**
   * Returns a merged {@link Arguments}.
   *
   * @param args
   */
  public final Arguments merge(Map<String, ?> args) {
    if ((args == null) || args.isEmpty()) {
      return this;
    } else if (this.arguments.isEmpty()) {
      return new Arguments(args);
    }

    Map<String, Object> map = new HashMap<>(this.arguments);
    args.forEach((name, value) -> map.put(name, value));
    return new Arguments(map);
  }
}
