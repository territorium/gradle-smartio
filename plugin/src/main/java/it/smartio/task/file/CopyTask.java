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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import it.smartio.common.env.EnvironmentUtil;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.file.FileTreeCopying;

/**
 * The {@link CopyTask} copies a directory or a file. In case of a directory the whole directory is
 * copied recursively.
 */
public class CopyTask implements Task {

  private final String source;
  private final String target;

  /**
   * Constructs an instance of {@link CopyTask}.
   *
   * @param source
   * @param target
   */
  public CopyTask(String source, String target) {
    this.source = source;
    this.target = target;
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
   * Handles the copy request.
   */
  @Override
  public final void handle(TaskContext context) throws IOException {
    Path sourcePath = toPath(this.source, context);
    Path targetPath = toPath(this.target, context);

    if (!sourcePath.toFile().exists()) {
      throw new RuntimeException("File '" + sourcePath + "' doesn't exist!");
    }

    boolean isDirectory = sourcePath.toFile().isDirectory();

    // Create directory if it not exists
    if (!targetPath.toFile().exists()) {
      File directory = targetPath.toFile();
      if (!isDirectory) {
        directory = directory.getParentFile();
      }
      directory.mkdirs();
    }

    if (isDirectory) {
      FileTreeCopying.copyFileTree(sourcePath, targetPath);
    } else {
      Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    context.getLogger().onInfo("Copied from '{}' to '{}'", sourcePath, targetPath);
  }
}
