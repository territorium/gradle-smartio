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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;


/**
 * The {@link BrandingTaskProperties} is used to create all Android & iOS branding details.
 */
public class BrandingTaskProperties implements Task {

  /**
   * Applies the sub-tasks to branding the applications for the customer.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) throws IOException {
    Properties properties = new Properties();
    properties.put("name", context.getEnvironment().get(Branding.NAME));
    properties.put("file", context.getEnvironment().get(Branding.FILE));
    properties.put("uri", context.getEnvironment().get(Branding.HOST));
    properties.put("model", context.getEnvironment().get(Branding.MODEL));
    properties.put("offline", context.getEnvironment().get(Branding.OFFLINE));
    properties.put("ios.id", context.getEnvironment().get(Branding.IOS));
    properties.put("android.id", context.getEnvironment().get(Branding.ANDROID));

    try (Writer writer = new FileWriter(new File(context.getWorkingDir(), "build.properties"))) {
      properties.store(writer, "Generated by smart.IO for Gradle");
    }
  }
}
