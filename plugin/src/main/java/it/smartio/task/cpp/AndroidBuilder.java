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

package it.smartio.task.cpp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import it.smartio.util.env.OS;


/**
 * The {@link AndroidBuilder} class.
 */
public class AndroidBuilder extends CppBuilder {

  private File    home;
  private String  target;
  private String  buildDir;
  private String  targetPlatform;

  private File    store;
  private String  alias;
  private String  password;
  private String  javaHome;
  private boolean enableAAB;


  private final String executable = OS.isWindows() ? "androiddeployqt.exe" : "androiddeployqt";

  /**
   * Constructs an instance of {@link AndroidBuilder}.
   *
   * @param workingDir
   */
  public AndroidBuilder(File workingDir) {
    super(workingDir);
    this.targetPlatform = "android-30";
  }

  /**
   * Sets the Qt home directory.
   */
  public final AndroidBuilder setHome(File home) {
    this.home = home;
    return this;
  }

  /**
   * Sets the Android target name.
   */
  public final AndroidBuilder setTarget(String target) {
    this.target = target;
    return this;
  }

  /**
   * Sets the Android target platform.
   */
  public final AndroidBuilder setTargetPlatform(String targetPlatform) {
    this.targetPlatform = targetPlatform;
    return this;
  }

  /**
   * Sets the Android key store.
   */
  public final AndroidBuilder setKeyStore(File keystore) {
    this.store = keystore;
    return this;
  }

  /**
   * Sets the Android store alias.
   */
  public final AndroidBuilder setAlias(String alias) {
    this.alias = alias;
    return this;
  }

  /**
   * Sets the Android store password.
   */
  public final AndroidBuilder setPassword(String password) {
    this.password = password;
    return this;
  }

  /**
   * Sets the Java Home.
   */
  public final AndroidBuilder setJavaHome(String javaHome) {
    this.javaHome = javaHome;
    return this;
  }

  /**
   * Sets the Android build directory.
   */
  public final AndroidBuilder setBuildDir(String buildDir) {
    this.buildDir = buildDir;
    return this;
  }

  /**
   * Enables the Android App Bundle.
   */
  public final AndroidBuilder enableAAB() {
    this.enableAAB = true;
    return this;
  }

  /**
   * Build the commands for the builder.
   */
  @Override
  protected void buildCommand(List<String> commands) {
    File androiddeployqt = this.home.toPath().resolve("bin").resolve(this.executable).toFile();
    if (!androiddeployqt.exists() || androiddeployqt.isDirectory()) {
      throw new RuntimeException("Invalid command: " + androiddeployqt);
    }

    commands.add(androiddeployqt.getAbsolutePath());
    commands.add("--input");
    commands.add("android-" + this.target + "-deployment-settings.json");
    commands.add("--output");
    commands.add(this.buildDir);
    commands.add("--android-platform");
    commands.add(this.targetPlatform);
    commands.add("--release");
    commands.add("--gradle");
    if (this.enableAAB) {
      commands.add("--aab");
    }
    commands.add("--sign");
    commands.add(this.store.getAbsolutePath());
    commands.add(this.alias);
    commands.add("--storepass");
    commands.add(this.password);
    if (this.javaHome != null) {
      commands.add("--jdk");
      commands.add(this.javaHome);
    }
  }

  /**
   * Find the Qt project file for the working directory. Otherwise throws an {@link IOException}.
   *
   * @param workingDir
   */
  public static File findProjectFile(File workingDir) throws IOException {
    Optional<File> file = Arrays.asList(workingDir.listFiles((d, n) -> n.endsWith(".pro"))).stream().findAny();
    if (file.isPresent()) {
      return file.get();
    }
    throw new IOException(String.format("The folder '%s' doesn't define a Qt project!", workingDir));
  }
}
