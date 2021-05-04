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
import java.util.List;

import it.smartio.common.env.Environment;
import it.smartio.common.env.EnvironmentUtil;

/**
 * The {@link FileMatcher} is an utility that get all files that match the path pattern. The
 * returned {@link FileMatcher}'s allow to replace the parameters of the input with the parameters
 * found on the matches.
 */
public class FileMatcher {

  private final File        file;
  private final Environment environment;

  /**
   * Constructs an instance of {@link FileMatcher}.
   *
   * @param file
   * @param environment
   */
  FileMatcher(File file, Environment environment) {
    this.file = file;
    this.environment = environment;
  }

  /**
   * Gets the {@link File}.
   */
  public final File getFile() {
    return this.file;
  }

  /**
   * Gets the named parameter.
   */
  public final Environment getEnvironment() {
    return this.environment;
  }

  /**
   * Replaces the indexed or named placeholder's with the the parameter values.
   *
   * @param pattern
   */
  public final String map(String pattern) {
    return EnvironmentUtil.replace(pattern, this.environment);
  }

  /**
   * Get the matching {@link File} as string.
   */
  @Override
  public String toString() {
    return getFile().toString();
  }

  /**
   * Resolve the input pattern on the working directory, to find all matching files.
   *
   * @param workingDir
   * @param pattern
   */
  public static List<FileMatcher> of(File workingDir, String pattern) throws IOException {
    return FilePattern.matches(workingDir, pattern);
  }
}