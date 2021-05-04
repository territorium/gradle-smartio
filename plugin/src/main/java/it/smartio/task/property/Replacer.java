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

package it.smartio.task.property;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.common.env.Environment;
import it.smartio.util.file.FilePattern;

/**
 * The {@link Replacer} class.
 */
public abstract class Replacer {

  private final String  file;
  private final Pattern pattern;

  /**
   * Constructs an instance of {@link Replacer}.
   *
   * @param file
   * @param pattern
   */
  protected Replacer(String file, String pattern) {
    this.file = Replacer.toFilePattern(file);
    this.pattern = Pattern.compile(pattern);
  }

  /**
   * Get the file pattern for the {@link Replacer}.
   */
  public String getFilePattern() {
    return this.file;
  }

  /**
   * Get the value from the environment variable.
   *
   * @param name
   * @param value
   * @param environment
   */
  protected abstract String getValue(String name, String value, Environment environment);

  /**
   * Replaces the keys with the provided environment variables.
   *
   * @param file
   * @param environment
   */
  public final String replace(File file, Environment environment, ChangeSet values) throws IOException {
    String input = new String(Files.readAllBytes(file.toPath()));

    int offset = 0;
    StringBuffer content = new StringBuffer();
    Matcher matcher = this.pattern.matcher(input);
    while (matcher.find()) {
      content.append(input.substring(offset, matcher.start()));
      if (matcher.groupCount() == 2) {
        String name = matcher.group(2);
        String value = getValue(name, null, environment);
        if (value != null) {
          content.append(value);
        }

        if (value != null) {
          values.add(name, null, value);
        }
      } else {
        content.append(matcher.group(1));

        String name = matcher.group(2);
        String valueOld = matcher.group(3);
        String valueNew = getValue(name, valueOld, environment);
        if (valueNew != null) {
          content.append(valueNew);
        }

        if ((valueNew != null) && !valueNew.equals(valueOld)) {
          values.add(name, valueOld, valueNew);
        }

        content.append(matcher.group(4));
      }
      offset = matcher.end();
    }

    content.append(input.substring(offset));
    return content.toString();
  }

  /**
   * Transform a file pattern to a regular expression.
   *
   * <pre>
   *   *.{pro,pri}
   * </pre>
   *
   * @param pattern
   */
  private static String toFilePattern(String pattern) {
    return String.format("^[^.]+%s$", FilePattern.toRegExp(pattern));
  }
}
