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

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.env.Environment;
import it.smartio.common.task.TaskContextAsync;
import it.smartio.gradle.config.PipelineConfig;
import it.smartio.gradle.config.StageConfig;
import it.smartio.task.TaskBuilder;
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
 * The {@link Pipeline} implements a factory to create the instances of the pipeline from the
 * {@link GradleConfig}.
 */
public class Pipeline {

  private static final String NAME_APP = "smartIO";

  private final GradleConfig  config;
  private final Project       project;
  private final TaskFactory   factory;

  /**
   * Constructs an instance of {@link Pipeline}.
   *
   * @param config
   * @param project
   */
  public Pipeline(GradleConfig config, Project project) {
    this.config = config;
    this.project = project;
    this.factory = Pipeline.newFactory();
  }

  /**
   * Parses the stages from the {@link GradleConfig}.
   *
   * @param name
   * @param stage
   */
  private Stream<StageConfig> getStages(String name, String stage) {
    Optional<PipelineConfig> optional = config.getPipelines().stream().filter(c -> c.name.equals(name)).findFirst();
    if (!optional.isPresent()) {
      project.getLogger().warn("Pipeline '{}' not found!", name);
      throw new RuntimeException(String.format("Pipeline '%s' not found!", name));
    }
    return optional.get().getStages().stream().filter(s -> s.name.equals(stage));
  }

  /**
   * Executes a pipeline from the {@link GradleConfig}.
   *
   * @param name
   * @param stage
   */
  public final void exec(String name, String stage) {
    Logger logger = project.getLogger();
    File workingDir = config.getWorkingDir();
    Environment environment = config.getEnvironment(logger, workingDir);

    Arguments arguments = new Arguments(config.getProject().getProperties());

    TaskBuilder builder = new TaskBuilder(name);
    getStages(name, stage).forEach(s -> {
      TaskBuilder taskBuilder = builder.addTask(s.name);

      s.cmds.forEach(c -> taskBuilder.addTask(c, factory.createTask(c, arguments.merge(s.args), workingDir)));
      s.getTasks().stream().filter(t -> QtPlatform.isSupported(t.device, environment)).forEach(t -> taskBuilder
          .addTask(t.name, factory.createTask(t.name, arguments.merge(s.args).merge(t.args), workingDir)));
    });

    logger.warn("Pipeline '{}-{}': starting...", name, stage);
    try (TaskContextAsync context = new GradleContext(logger, workingDir, environment)) {
      builder.build().handle(context);
      logger.warn("Pipeline '{}-{}': completed!", name, stage);
    } catch (Throwable e) {
      logger.error("Pipeline '{}-{}': terminated!", name, stage, e);
      throw new RuntimeException(e);
    }
  }

  public static TaskFactory newFactory() {
    TaskFactory factory = new TaskFactory();

    factory.add("copy", (a, w) -> new CopyTask(a.get("source"), a.get("target")))
        .setDescription("Copies files or folders from source to target.");

    factory.add("replace", (a, w) -> {
      if (!a.contains("pattern", "variables")) {
        return new PropertyTask();
      }

      String pattern = a.get("pattern");
      List<String> variables = a.get("variables");
      return new PropertyTask(pattern, variables);
    }).setDescription("Replaces all contents by pattern, using the provided variables.");

    factory.add("revision", (a, w) -> c -> {
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
    }).setDescription("Applies the GIT revision number to the files");;

    factory.add("archive", (a, w) -> new ArchiveTask(a.get("archive"), a.asList("sources")))
        .setDescription("Archive the sources to the specified archiver file.");

    factory.add("qmake", (a, w) -> {
      String module = a.get("module");
      File projectDir = new File(w, module);
      if (!projectDir.exists()) {
        projectDir = w;
      }

      File projectFile = new File(projectDir, module + ".pro");
      return c -> {
        Set<QtPlatform> platforms = QtPlatform.getPlatforms(c.getEnvironment());
        platforms.stream().map(p -> new QMakeTask(p, module, projectFile)).forEach(t -> {
          try {
            t.handle(c);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      };
    }).setDescription("Invokes the QMake command to the defined modules.");

    factory.add("clean", (a, w) -> c -> {
      String module = a.get("module");
      Set<QtPlatform> platforms = QtPlatform.getPlatforms(c.getEnvironment());
      platforms.stream().map(p -> new MakeTask(p, module, "clean")).forEach(t -> {
        try {
          t.handle(c);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }).setDescription("Clean the modules");

    factory.add("make", (a, w) -> c -> {
      String module = a.get("module");
      Set<QtPlatform> platforms = QtPlatform.getPlatforms(c.getEnvironment());
      platforms.stream().map(p -> new MakeTask(p, module)).forEach(t -> {
        try {
          t.handle(c);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }).setDescription("Invoke the make command on the modules");

    factory.add("install", (a, w) -> c -> {
      String module = a.get("module");
      Set<QtPlatform> platforms = QtPlatform.getPlatforms(c.getEnvironment());
      platforms.stream()
          .map(p -> p.isAndroid() ? new AndroidInstallTask(p, module) : new MakeTask(p, module, "install"))
          .forEach(t -> {
            try {
              t.handle(c);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
    }).setDescription("Installs the modules to the target directory.");

    factory.add("android", (a, w) -> new AndroidDeployTask(NAME_APP, a.get("module")))
        .setDescription("Create an android AAB/APK");
    factory.add("xcarchive", (a, w) -> new XCArchiveTask(NAME_APP, a.get("module")))
        .setDescription("Creates the iOS archive");
    factory.add("xcexport", (a, w) -> new IPADeployTask(NAME_APP, a.get("module")))
        .setDescription("Exports the archive to an IPA");

    factory.add("branding", (a, w) -> new BrandingTask(a.get("source"), a.get("target")))
        .setDescription("Generates the braning informations");

    factory.add("package", (a, w) -> new PackageTask(a.get("source"), a.get("packages"), a.get("artifacts"),
        a.get("modulePath"), a.asList("modules"))).setDescription("Creates the repository packages");
    factory.add("repogen", (a, w) -> new RepositoryTask(a.get("packages"), a.get("repository")))
        .setDescription("Generates the repository for the installer");

    factory.add("shell", (a, w) -> new ShellTask(a, w)).setDescription("Invokes a shell command");

    return factory;
  }
}
