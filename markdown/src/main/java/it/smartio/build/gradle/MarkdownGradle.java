/*
 * Copyright (c) 2001-2024 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.build.gradle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import it.smartio.build.DocumentBuilder;
import it.smartio.commonmark.MarkdownReader;
import it.smartio.docs.util.Replacer;

public class MarkdownGradle implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    MarkdownGradleConfig config = project.getExtensions().create("markdown", MarkdownGradleConfig.class);

    project.task("markdown").doLast(task -> buildMarkdown(config, project));
    project.task("md-merge").doLast(task -> mergeMarkdown(config, project));
  }

  /**
   * Generates the PDF from the markdown.
   *
   * @param gradle
   * @param project
   */
  private void buildMarkdown(MarkdownGradleConfig gradle, Project project) {
    String config = gradle.config;
    if (!config.isEmpty() && !config.startsWith(":") && !config.startsWith("/")) {
      config = new File(project.getRootDir(), config).getAbsolutePath();
    }

    DocumentBuilder builder = new DocumentBuilder(project.getRootDir());
    builder.setConfig(config);
    builder.setSource(gradle.source);
    builder.setTarget(project.getBuildDir());

    builder.onInfo(m -> project.getLogger().info(m));
    builder.onError(t -> project.getLogger().error("An Error occured!", t));
    builder.addProperties(System.getProperties());
    project.getProperties().entrySet().stream().filter(e -> e.getValue() != null)
        .forEach(e -> builder.addProperty(e.getKey(), e.getValue()));

    // Adding GIT informations to the filename
    builder.setSuffix(getFileSuffix(builder.getProperties()));

    builder.build();
  }

  /**
   * Generates the PDF from the markdown.
   *
   * @param gradle
   * @param project
   */
  private void mergeMarkdown(MarkdownGradleConfig gradle, Project project) {
    // Load properties
    Properties properties = new Properties();
    project.getProperties().entrySet().stream().filter(e -> e.getValue() != null)
        .forEach(e -> properties.put(e.getKey(), e.getValue()));
    properties.putAll(System.getProperties());
    Replacer replacer = new Replacer(properties);

    // Collect all markdown files
    List<File> files = new ArrayList<>();
    File source = new File(project.getRootDir(), gradle.source);
    if (source.isDirectory()) {
      for (File file : source.listFiles()) {
        if (file.getName().toLowerCase().endsWith(".md")) {
          files.add(file);
        }
      }
    } else if (source.getName().toLowerCase().endsWith(".md")) {
      files.add(source);
    }

    for (File file : files) {
      MarkdownReader reader = new MarkdownReader(file);
      File target = new File(project.getBuildDir(), file.getName());
      try (Writer writer = new FileWriter(target)) {
        writer.write(replacer.replaceAll(reader.readAll()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets the file-suffix from the {@link Properties}.
   *
   * @param properties
   */
  private static String getFileSuffix(Properties properties) {
    if (properties.containsKey("GIT_VERSION")) {
      String suffix = "-" + properties.get("GIT_VERSION");
      if (properties.containsKey("BUILD_NUMBER")) {
        suffix += "+" + properties.get("BUILD_NUMBER");
      }
      return suffix;
    } else if (properties.containsKey("git.version")) {
      String suffix = "-" + properties.get("git.version");
      if (properties.containsKey("git.buildnumber")) {
        suffix += "+" + properties.get("git.buildnumber");
      }
      return suffix;
    }
    return null;
  }
}
