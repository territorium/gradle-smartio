/*
 * Copyright (c) 2001-2024 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.docs.util;

import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link Replacer} class.
 */
public class Replacer {

  // Environment variables: {{ENVIRONMENT_VARIABLE;FORMATTER}}
  private static final String  PATTERN  = "\\{\\{([^};]+)(?:;([^}]+))?}}";
  private static final Pattern TEMPLATE = Pattern.compile(Replacer.PATTERN, Pattern.CASE_INSENSITIVE);


  private final Properties properties;

  /**
   * Constructs an instance of {@link Replacer}.
   *
   * @param properties
   */
  public Replacer(Properties properties) {
    this.properties = properties;
  }

  /**
   * Get an environment variable by name.
   *
   * @param name
   * @param format
   */
  private final String getProperty(String name, String format) {
    if (this.properties.containsKey(name)) {
      return this.properties.getProperty(name);
    }
    return "{{" + name + ((format == null) ? "" : ";" + format) + "}}";
  }

  /**
   * Replace all environment variables on the text
   *
   * @param text
   */
  public final String replaceAll(String text) {
    return Replacer.replaceAll(text, Replacer.TEMPLATE, m -> getProperty(m.group(1), m.group(2)));
  }

  /**
   * Uses the {@link Pattern} to replace parts of the text.
   *
   * @param text
   * @param pattern
   * @param function
   */
  public static String replaceAll(String text, Pattern pattern, Function<Matcher, String> function) {
    StringBuilder content = new StringBuilder();
    Matcher matcher = pattern.matcher(text);

    int offset = 0;
    while (matcher.find()) {
      String result = function.apply(matcher);
      if (result != null) {
        content.append(text.substring(offset, matcher.start()));
        content.append(result);
        offset = matcher.end();
      }
    }
    content.append(text.substring(offset));
    return content.toString();
  }
}
