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
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import it.smartio.build.Build;
import it.smartio.common.env.Environment;
import it.smartio.common.env.EnvironmentVariables;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.task.property.PropertyTask;
import it.smartio.util.version.Version;


/**
 * The {@link BrandingTask} is used to create all Android & iOS branding details.
 */
public class BrandingTask implements Task {

  private final String source;
  private final String target;

  /**
   * Constructs an instance of {@link BrandingTask}.
   *
   * @param source
   * @param target
   */
  public BrandingTask(String source, String target) {
    this.source = source;
    this.target = target;
  }

  /**
   * Applies the sub-tasks to branding the applications for the customer.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext c) {
    File workingDir = this.target == null ? c.getWorkingDir() : new File(c.getWorkingDir(), this.target);

    Map<String, String> vars = new HashMap<>();

    try {
      if (this.source != null) {
        File sourceDir = new File(this.source);
        List<String> fileNames =
            Arrays.asList("build.properties", "icon.svg", "logo.svg", "icon.png", "logo.png", "splash.jpg");
        for (String filename : fileNames) {
          File file = new File(sourceDir, filename);
          if (file.exists()) {
            Files.copy(file.toPath(), new File(workingDir, filename).toPath(), StandardCopyOption.REPLACE_EXISTING);
          }
        }

        File pixometer = new File(sourceDir, "pixometerlicense.txt");
        if (pixometer.exists()) {
          File targetDir = new File(workingDir, "android/res/raw");
          targetDir.mkdirs();
          Files.copy(pixometer.toPath(), new File(targetDir, "pixometerlicense.txt").toPath(),
              StandardCopyOption.REPLACE_EXISTING);
        }
      }

      Properties properties = new Properties();
      properties.load(new FileReader(new File(workingDir, "build.properties")));

      vars.put(Branding.NAME, properties.getProperty("name"));
      vars.put(Branding.FILE, properties.getProperty("file"));
      vars.put(Branding.ICON, "icon");
      vars.put(Branding.LOGO, "logo");
      vars.put(Branding.SPLASH, "splash");
      vars.put(Branding.BACKGROUND,
          properties.containsKey("bgcolor") ? (String) properties.getProperty("bgcolor") : "#ffffff");

      vars.put(Branding.HOST, properties.getProperty("uri"));
      vars.put(Branding.MODEL, properties.getProperty("model"));
      vars.put(Branding.OFFLINE, properties.getProperty("offline"));
      vars.put(Branding.ANDROID, properties.getProperty("android.id"));
      vars.put(Branding.IOS, properties.getProperty("ios.id"));

      vars.put(Build.PRODUCT_RESOURCE, properties.getProperty("uri"));
      vars.put(Build.PRODUCT_MODEL, properties.getProperty("model"));
      vars.put(Build.PRODUCT_OFFLINE, properties.getProperty("offline"));
      vars.put(Build.ANDROID_ID, properties.getProperty("android.id"));
      vars.put(Build.IOS_EXPORT_ID, properties.getProperty("ios.id"));


      if (properties.containsKey("buildnumber")) {
        try {
          int buildnumber = Integer.parseInt(properties.getProperty("buildnumber"));
          Version version = Version.of(c.getEnvironment().get(Build.REVISION));
          version = version.build(Integer.parseInt(version.getBuild()) + buildnumber);

          vars.put(Build.BUILDNUMBER, version.getBuild());
          vars.put(Build.REVISION, version.toString("0.0.0+0")); // Used by IPA,AAB & APK
        } catch (Throwable e) {}
      }


      Environment env = new EnvironmentVariables(vars, c.getEnvironment());
      new PropertyTask().handle(c.wrap(env));

      TaskContext context = c.wrap(workingDir, env);
      new BrandingTaskAndroid().handle(context);
      new BrandingTaskIOS().handle(context);
    } catch (IOException e) {
      c.getLogger().onError(e, e.getMessage());
    }
  }
}
