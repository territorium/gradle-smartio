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

package it.smartio.task.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import it.smartio.common.env.EnvironmentUtil;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.archive.Assembly;


/**
 * The {@link ArchiveTask} class.
 */
public class ArchiveTask implements Task {

  private final String       archive;
  private final List<String> patterns;

  /**
   * Constructs an instance of {@link ArchiveTask}.
   *
   * @param archive
   * @param patterns
   */
  public ArchiveTask(String archive, List<String> patterns) {
    this.archive = archive;
    this.patterns = patterns;
  }

  /**
   * Converts the path name to a {@link Path}, replacing environment variables.
   *
   * @param pathname
   * @param context
   */
  protected final Path toPath(String pathname, TaskContext context) {
    Path path = Paths.get(EnvironmentUtil.replace(pathname, context.getEnvironment()));
    return path.isAbsolute() ? path : context.getWorkingDir().toPath().resolve(path);
  }

  /**
   * Invokes the {@link Task}.
   *
   * @param context
   */
  @Override
  public void handle(TaskContext context) throws IOException {
    Path path = toPath(this.archive, context);

    Assembly assembly = Assembly.of(context.getWorkingDir());
    assembly.setArchive(path.toFile());
    for (String pattern : this.patterns) {
      assembly.addPattern(EnvironmentUtil.replace(pattern, context.getEnvironment()).trim());
    }

    context.getLogger().onInfo("Create Archive: '{}'", assembly.archive());
    assembly.build(m -> context.getLogger().onInfo(m));
  }
}
