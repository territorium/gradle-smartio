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

package it.smartio.util.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.common.env.Environment;
import it.smartio.common.env.EnvironmentUtil;

/**
 * The {@link FilePattern} class.
 */
public class FilePattern extends SimpleFileVisitor<Path> {

  private final Path              workingPath;

  private final Pattern           pattern;
  private final Set<String>       names;
  private final List<FileMatcher> mappers = new ArrayList<>();

  /**
   * Constructs an instance of {@link FilePattern}.
   *
   * @param workingDir
   * @param pattern
   */
  private FilePattern(File workingDir, String pattern) {
    this.workingPath = workingDir.toPath();
    this.pattern = Pattern.compile("^" + pattern + "$");
    this.names = EnvironmentUtil.parseGroupNames(pattern);
  }

  /**
   * Gets the list of {@link FileMatcher}.
   */
  public final List<FileMatcher> getMappers() {
    return this.mappers;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
    return visitPath(path, attrs);
  }

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
    return visitPath(path, attrs);
  }

  private FileVisitResult visitPath(Path path, BasicFileAttributes attrs) {
    String input = this.workingPath.relativize(path).toString();
    Matcher matcher = this.pattern.matcher(input.replace('\\', '/')); // for windows matches
    if (matcher.find()) {
      Environment e = FilePattern.getParameters(matcher, this.names);
      this.mappers.add(new FileMatcher(path.toFile(), e));
      return FileVisitResult.SKIP_SUBTREE;
    }
    return FileVisitResult.CONTINUE;
  }

  /**
   * Get the indexed parameters from the matcher.
   *
   * @param matcher
   * @param names
   */
  private static Environment getParameters(Matcher matcher, Set<String> names) {
    Map<String, String> params = new HashMap<>();
    params.put(Integer.toString(0), matcher.group(0));
    for (int index = 0; index < matcher.groupCount(); index++) {
      params.put(Integer.toString(index + 1), matcher.group(index + 1));
    }
    for (String name : names) {
      params.put(name, matcher.group(name));
    }
    return Environment.of(params);
  }

  /**
   * Converts the file pattern to a regular expression.
   *
   * @param pattern
   */
  public static String toRegExp(String pattern) {
    return pattern.replace(".", "\\.").replace("*", ".*").replace(",", "|").replace("{", "(").replace("}", ")");
  }

  /**
   * Copy the file tree using the environment variables.
   *
   * @param workingDir
   * @param pattern
   */
  public static List<FileMatcher> matches(File workingDir, String pattern) throws IOException {
    FilePattern visitor = new FilePattern(workingDir, pattern);
    Files.walkFileTree(workingDir.toPath(), visitor);
    return visitor.getMappers();
  }
}