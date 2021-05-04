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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link EnvironmentUtil} class.
 */
public abstract class EnvironmentUtil {

  private static final Pattern NAMES  = Pattern.compile("\\(\\?<([a-z][a-z_0-9]*)>", Pattern.CASE_INSENSITIVE);
  private static final Pattern PARAMS = Pattern.compile("\\$([0-9]+|[a-z][a-z_0-9]*)", Pattern.CASE_INSENSITIVE);

  /**
   * Constructs an instance of {@link EnvironmentUtil}.
   */
  private EnvironmentUtil() {}

  /**
   * Parses the group names from the pattern.
   *
   * @param pattern
   */
  public static Set<String> parseGroupNames(String pattern) {
    Set<String> names = new HashSet<>();
    Matcher matcher = EnvironmentUtil.NAMES.matcher(pattern);
    while (matcher.find()) {
      names.add(matcher.group(1));
    }
    return names;
  }

  /**
   * Replaces the indexed or named placeholder's with the the parameter values.
   *
   * @param pattern
   * @param environment
   */
  public static String replace(String pattern, Environment environment) {
    StringBuffer buffer = new StringBuffer();
    int offset = 0;

    Matcher matcher = EnvironmentUtil.PARAMS.matcher(pattern);
    while (matcher.find()) {
      String name = matcher.group(1);
      String value = environment.get(name);
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
}
