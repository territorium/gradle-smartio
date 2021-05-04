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

package it.smartio.util.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link Assembly} class.
 */
public class Assembly {

  private static final Pattern PATTERN_FILE = Pattern.compile("\\{([^}]+)\\}");
  private static final Pattern PATTERN_PATH = Pattern.compile("^([^\\{=>\\s]*)(?:/([^=>\\s]+))?(?:\\s*=>\\s*(.+))?$");


  private final File workingDir;


  private File               archive;
  private final List<String> patterns = new ArrayList<>();

  /**
   * Constructs an instance of {@link Assembly}.
   *
   * @param workingDir
   */
  private Assembly(File workingDir) {
    this.workingDir = workingDir;
  }

  /**
   * Get the archive
   */
  public final File archive() {
    return this.archive;
  }

  /**
   * Set the archive
   *
   * @param archive
   */
  public final Assembly setArchive(File archive) {
    this.archive = archive;
    return this;
  }

  /**
   * Set the archive
   *
   * @param pattern
   */
  public final Assembly setSources(String pattern) {
    for (String p : pattern.split("[\\n]")) {
      addPattern(p.trim());
    }
    return this;
  }

  /**
   * Set the archive
   *
   * @param pattern
   */
  public final Assembly addPattern(String pattern) {
    this.patterns.add(pattern);
    return this;
  }

  /**
   * Build the archive
   */
  public final void build(Consumer<String> consumer) throws IOException {
    try (ArchiveBuilder builder = Archive.builder(this.archive)) {
      for (String input : this.patterns) {
        Matcher matcher = Assembly.PATTERN_PATH.matcher(input);
        if (matcher.find()) {
          File file = new File(this.workingDir, matcher.group(1));
          if (!file.exists()) {
            file = new File(matcher.group(1));
          }
          if (!file.exists()) {
            throw new IOException("File '" + matcher.group(1) + "' does not exist");
          }

          if (matcher.group(2) != null) {
            for (String sourcePath : Assembly.parsePatterns(matcher.group(2))) {
              builder.addFile(file, sourcePath, matcher.group(3));
            }
          } else if (!file.isDirectory()) {
            builder.addFile(file.getParentFile(), file, matcher.group(3));
          } else {
            builder.addDirectory(file);
          }
        } else {
          throw new IOException("Couldn't find pattern '" + input + "'");
        }
      }
    }
  }

  /**
   * Parses all combinations of ile patterns.
   *
   * @param text
   */
  private static List<String> parsePatterns(String text) {
    List<String> files = new ArrayList<>();
    Matcher m = Assembly.PATTERN_FILE.matcher(text);

    int offset = 0;
    while (m.find()) {
      if (m.start() > offset) {
        Assembly.appendPatternPart(files, text.substring(offset, m.start()));
      }
      Assembly.appendPatternPart(files, m.group(1).split(","));
      offset = m.end();
    }
    if (offset < text.length()) {
      Assembly.appendPatternPart(files, text.substring(offset));
    }
    return files;
  }

  /**
   * Append the parts to the current pattern
   *
   * @param patterns
   * @param parts
   */
  private static void appendPatternPart(List<String> patterns, String... parts) {
    if (patterns.isEmpty()) {
      patterns.addAll(Arrays.asList(parts));
    } else {
      List<String> list = new ArrayList<>();
      for (String part : parts) {
        patterns.stream().forEach(p -> list.add(p + part));
      }
      patterns.clear();
      patterns.addAll(list);
    }
  }

  /**
   * Constructs an instance of {@link Assembly}.
   *
   * @param workingDir
   */
  public static Assembly of(File workingDir) {
    return new Assembly(workingDir);
  }
}
