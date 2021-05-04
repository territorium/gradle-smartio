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

package it.smartio.task.repo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.common.env.Environment;
import it.smartio.common.env.EnvironmentUtil;
import it.smartio.util.archive.Archive;
import it.smartio.util.file.FileMatcher;
import it.smartio.util.file.FileTreeCopying;
import it.smartio.util.file.FileTreeLastModified;
import it.smartio.util.version.Version;

/**
 * The {@link PackageData} provides information about the data/ folder of a package. The
 * {@link PackageData} uses the module name and the data file to generate the structure on the
 * target folder
 */
final class PackageData {

  private static final Pattern REPLACER = Pattern.compile("^([^/]*)/([^/]+)/([^/]+)/(.*)$");
  private static final Pattern RELEASE  = Pattern.compile("(?:\\$\\{|\\{\\{\\$)RELEASE;([0.-]+)\\}\\}?");


  private final String      name;
  private final String      module;
  private final String      source;
  private final String      target;

  private final File        workingDir;
  private final Environment environment;

  /**
   * Constructs an instance of {@link PackageData}.
   *
   * @param name
   * @param source
   * @param target
   * @param builder
   */
  public PackageData(String name, String source, String target, PackageBuilder builder) {
    this.name = PackageData.toName(name, builder.getEnvironment());
    this.module = name;
    this.source = source;
    this.target = target;
    this.workingDir = builder.getArtifactsDir();
    this.environment = builder.getEnvironment();
  }

  /**
   * Removes the pattern from the module name.
   *
   * @param name
   */
  private static String toName(String name, Environment environment) {
    Matcher m = PackageData.REPLACER.matcher(name);
    if (!m.find()) {
      return name;
    }

    String value = EnvironmentUtil.replace(m.group(3), environment).replaceAll("[.-]", "");
    return String.format("%s%s%s", m.group(1), value, m.group(4));
  }

  /**
   * Removes the pattern from the module name.
   *
   * @param data
   */
  public String remap(String data) {
    Matcher matcher = PackageData.REPLACER.matcher(this.module);
    if (!matcher.find()) {
      return data;
    }

    String pattern = String.format("%s(%s)", matcher.group(1).replace(".", "\\."), matcher.group(2));
    String value = EnvironmentUtil.replace(matcher.group(3), this.environment);
    Version version = Version.parse(value);

    matcher = PackageData.RELEASE.matcher(data);
    if (matcher.find()) {
      data = matcher.replaceFirst(version.toString(matcher.group(1)));
    }

    data = EnvironmentUtil.replace(data, this.environment);
    value = value.replaceAll("[.-]", "");

    int offset = 0;
    StringBuffer buffer = new StringBuffer();
    matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(data);
    while (matcher.find()) {
      buffer.append(data.substring(offset, matcher.start(1)));
      buffer.append(value);
      offset = matcher.end(1);
    }
    buffer.append(data.substring(offset, data.length()));
    return buffer.toString();
  }

  /**
   * Gets the package name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the source data file.
   */
  public File getWorkingDir() {
    return this.workingDir;
  }

  /**
   * Gets the source location.
   */
  public String getSource() {
    return this.source;
  }

  /**
   * Gets the target location.
   */
  public String getTarget() {
    return (this.target == null) ? "" : this.target;
  }

  /**
   * Gets the target location.
   */
  public String getTarget(String suffix) {
    String location = getTarget();
    if (suffix != null) {
      location = Paths.get(location, suffix).toString();
    }
    return location;
  }


  /**
   * Build the /data folder for the package.
   *
   * @param workingDir
   * @param environment
   */
  public void build(File workingDir, Environment environment) throws IOException {
    Map<String, String> map = new HashMap<>();
    Environment env = environment.map(map);
    LocalDate releaseDate = null;

    String subPath = null;
    String source = getSource();
    if (source.contains("!")) {
      subPath = source.substring(source.lastIndexOf("!") + 1);
      source = source.substring(0, source.lastIndexOf("!"));
    }

    Path workingPath = workingDir.toPath().resolve(getName()).resolve(PackageBuilder.DATA);
    for (FileMatcher matcher : FileMatcher.of(getWorkingDir(), source)) {
      map.putAll(matcher.getEnvironment().toMap());

      String target = getTarget();
      if (target.isEmpty() && !matcher.getFile().isDirectory()) {
        target = matcher.getFile().getName();
      }
      target = EnvironmentUtil.replace(target, env);

      // Copy the data to the build
      Path targetPath = workingPath.resolve(matcher.map(target));
      targetPath.toFile().getParentFile().mkdirs();

      try {
        Archive archive = Archive.of(matcher.getFile());
        archive.extractTo(targetPath.toFile(), subPath == null ? null : Paths.get(subPath));
      } catch (UnsupportedEncodingException e) {
        FileTreeCopying.copyFileTree(matcher.getFile().toPath(), targetPath);
      }

      LocalDate date = FileTreeLastModified.lastModified(targetPath);
      if ((releaseDate == null) || releaseDate.isBefore(date)) {
        releaseDate = date;
      }
    }

    // TODO Release date should always be calculated.
    if (releaseDate == null) {
      releaseDate = LocalDate.now();
    }

    // Change the package info
    PackageInfo info = new PackageInfo(env);
    info.updatePackageInfo(getName(), releaseDate, workingDir);
  }
}
