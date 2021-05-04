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

package it.smartio.task.product;

import java.io.File;
import java.io.IOException;

import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.file.FileSystem;


/**
 * The {@link BrandingTaskPlatform} is used to create all Android & iOS branding details.
 */
public abstract class BrandingTaskPlatform implements Task {

  /**
   * Applies the sub-tasks to branding the applications for the customer.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) throws IOException {
    String iconName = context.getEnvironment().get(Branding.ICON);
    String logoName = context.getEnvironment().get(Branding.LOGO);
    File icon = FileSystem.getFile(new File(iconName + ".png"), context.getWorkingDir());
    if (!icon.exists()) {
      icon = FileSystem.getFile(new File(iconName + ".svg"), context.getWorkingDir());
    }
    File logo = FileSystem.getFile(new File(logoName + ".png"), context.getWorkingDir());
    if (!logo.exists()) {
      logo = FileSystem.getFile(new File(logoName + ".svg"), context.getWorkingDir());
    }
    String background = context.getEnvironment().get(Branding.BACKGROUND);
    String bgColor = background == null ? "#ffffff" : background;

    render(logo, icon, background, bgColor, context);
  }


  protected abstract void render(File logo, File icon, String background, String bgColor, TaskContext context)
      throws IOException;
}
