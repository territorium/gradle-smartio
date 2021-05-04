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

package it.smartio.gradle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.task.TaskFactory;
import it.smartio.task.cpp.AndroidDeployTask;
import it.smartio.task.cpp.AndroidInstallTask;
import it.smartio.task.cpp.MakeTask;
import it.smartio.task.cpp.QMakeTask;
import it.smartio.task.file.ArchiveTask;
import it.smartio.task.file.CopyTask;
import it.smartio.task.product.BrandingTask;
import it.smartio.task.property.PropertyTask;
import it.smartio.task.repo.PackageTask;
import it.smartio.task.repo.RepositoryTask;
import it.smartio.task.shell.ShellTask;
import it.smartio.task.xcode.IPADeployTask;
import it.smartio.task.xcode.XCArchiveTask;
import it.smartio.util.file.FileSystem;
import it.smartio.util.git.Repository;
import it.smartio.util.git.RepositoryBuilder;
import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;

/**
 * The {@link GradleFactory} implements a factory to create the instances of the pipeline from the
 * {@link GradleConfig}.
 */
public class GradleFactory extends TaskFactory {

  /**
   * Constructs an instance of {@link GradleFactory}.
   *
   * @param constructors
   */
  private GradleFactory(Map<String, Constructor> constructors) {
    super(constructors);
  }

  /**
   * Creates a new instance of {@link GradleFactory}.
   */
  public static GradleFactory create() {
    Map<String, Constructor> constructors = new HashMap<>();

    constructors.put("copy", (a, w) -> new CopyTask(a.get("source"), a.get("target")));

    constructors.put("replace", (a, w) -> {
      if (!a.contains("pattern", "variables")) {
        return new PropertyTask();
      }

      String pattern = a.get("pattern");
      List<String> variables = a.get("variables");
      return new PropertyTask(pattern, variables);
    });

    constructors.put("revision", (a, w) -> {
      return c -> {
        File workingDir = c.getWorkingDir();
        RepositoryBuilder builder = new RepositoryBuilder(workingDir);

        try (Repository repo = builder.enableMonitor().build()) {
          Revision revision = repo.getRevision(Version.NONE);
          File targetDir = new File(c.getEnvironment().get(Build.TARGET_DIR));
          FileSystem.delete(targetDir);
          targetDir.mkdirs();
          File file = new File(targetDir, "revision");
          try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.write(revision.toString("0.0.0+0"));
          }

          repo.catchAndThrow();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      };
    });

    constructors.put("archive", (a, w) -> {
      String pattern = a.get("archive");
      List<String> sources = a.asList("sources");
      return new ArchiveTask(pattern, sources);
    });

    constructors.put("qmake", (a, w) -> {
      String module = a.get("module");
      File projectDir = new File(w, module);
      if (!projectDir.exists()) {
        projectDir = w;
      }

      File projectFile = new File(projectDir, module + ".pro");
      return new Task() {

        @Override
        public void handle(TaskContext context) throws IOException {
          Set<QtPlatform> platforms = QtPlatform.getPlatforms(context.getEnvironment());
          platforms.stream().map(p -> new QMakeTask(p, module, projectFile)).forEach(t -> {
            try {
              t.handle(context);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
        }
      };
    });

    constructors.put("clean", (a, w) -> {
      String module = a.get("module");
      return new Task() {

        @Override
        public void handle(TaskContext context) throws IOException {
          Set<QtPlatform> platforms = QtPlatform.getPlatforms(context.getEnvironment());
          platforms.stream().map(p -> new MakeTask(p, module, "clean")).forEach(t -> {
            try {
              t.handle(context);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
        }
      };
    });

    constructors.put("make", (a, w) -> {
      String module = a.get("module");
      return new Task() {

        @Override
        public void handle(TaskContext context) throws IOException {
          Set<QtPlatform> platforms = QtPlatform.getPlatforms(context.getEnvironment());
          platforms.stream().map(p -> new MakeTask(p, module)).forEach(t -> {
            try {
              t.handle(context);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
        }
      };
    });

    constructors.put("install", (a, w) -> {
      String module = a.get("module");
      return new Task() {

        @Override
        public void handle(TaskContext context) throws IOException {
          Set<QtPlatform> platforms = QtPlatform.getPlatforms(context.getEnvironment());
          platforms.stream()
          .map(p -> p.isAndroid() ? new AndroidInstallTask(p, module) : new MakeTask(p, module, "install"))
          .forEach(t -> {
            try {
              t.handle(context);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
        }
      };
    });

    constructors.put("android", (a, w) -> new AndroidDeployTask(BuildPlugin.NAME_APP, a.get("module")));
    constructors.put("xcarchive", (a, w) -> new XCArchiveTask(BuildPlugin.NAME_APP, a.get("module")));
    constructors.put("xcexport", (a, w) -> new IPADeployTask(BuildPlugin.NAME_APP, a.get("module")));

    constructors.put("branding", (a, w) -> new BrandingTask(a.get("source"), a.get("target")));

    constructors.put("package", (a, w) -> new PackageTask(a.get("source"), a.get("packages"), a.get("artifacts"),
        a.get("modulePath"), a.asList("modules")));
    constructors.put("repogen", (a, w) -> new RepositoryTask(a.get("packages"), a.get("repository")));


    constructors.put("shell", (a, w) -> new ShellTask(a, w));

    return new GradleFactory(constructors);
  }
}
