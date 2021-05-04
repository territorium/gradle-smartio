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

import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.file.FileSystem;

/**
 * The {@link DeleteTask} deletes the directory recursively and creates a new empty directory.
 */
public class DeleteTask implements Task {

  private final File directory;

  /**
   * Constructs an instance of {@link DeleteTask}.
   *
   * @param directory
   */
  public DeleteTask(File directory) {
    this.directory = directory;
  }

  /**
   * Gets the directory.
   */
  protected final File getDirectory() {
    return this.directory;
  }

  /**
   * Deletes the directory
   */
  @Override
  public final void handle(TaskContext context) {
    if (getDirectory().exists()) {
      FileSystem.delete(getDirectory());
    }
    getDirectory().mkdirs();
  }
}
