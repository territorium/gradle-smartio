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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * The {@link FileSystem} class.
 */
public abstract class FileSystem {

  /**
   * Constructs an instance of {@link FileSystem}.
   */
  private FileSystem() {}

  /**
   * Delete the {@link File} or the whole directory and all its files.
   *
   * @param file
   */
  public static boolean delete(File file) {
    if (file.isDirectory()) {
      if (!file.exists()) {
        return false;
      }

      try {
        Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        return true;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return file.delete();
  }

  /**
   * Get an absolute {@link File}.
   *
   * @param path
   */
  public static File getFile(String path, File workingDir) {
    return FileSystem.getFile(new File(path), workingDir);
  }

  /**
   * Get an absolute {@link File}.
   *
   * @param file
   */
  public static File getFile(File file, File workingDir) {
    return file.isAbsolute() ? file : workingDir.toPath().resolve(file.toPath()).toFile();
  }
}
