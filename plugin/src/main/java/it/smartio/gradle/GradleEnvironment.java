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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Properties;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.env.Environment;
import it.smartio.common.env.EnvironmentVariables;
import it.smartio.util.env.OS;
import it.smartio.util.git.Repository;
import it.smartio.util.git.RepositoryBuilder;
import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;


/**
 * The {@link GradleEnvironment} provides additional environment variables().
 */
public class GradleEnvironment extends EnvironmentVariables {

  private static final String GO_PIPELINE_ROOT         = "GO_PIPELINE_ROOT";
  private static final String GO_PIPELINE_COUNTER      = "GO_PIPELINE_COUNTER";
  private static final String GO_PIPELINE_COUNTER_BASE = "GO_PIPELINE_COUNTER_BASE";

  /**
   * Constructs an instance of {@link GradleEnvironment}.
   *
   * @param environment
   */
  private GradleEnvironment(Environment environment) {
    super(new LinkedHashMap<>(), environment);
  }

  /**
   * Creates the default environment variables for the 'build', 'target' and 'artifacts'
   * directories. If the gradle-plugin is executed in a GoCD environment, the defaults will be
   * created relative to the GoCD pipeline.
   *
   * @param workingDir
   * @param environment
   */
  private void parseDefaults(File workingDir, Environment environment) {
    if (environment.isSet(GradleEnvironment.GO_PIPELINE_ROOT)) {
      workingDir = new File(environment.get(GradleEnvironment.GO_PIPELINE_ROOT));
    }
    if (environment.isSet(GradleEnvironment.GO_PIPELINE_COUNTER)) {
      String counter = environment.get(GradleEnvironment.GO_PIPELINE_COUNTER);
      if (environment.isSet(GradleEnvironment.GO_PIPELINE_COUNTER_BASE)) {
        String base = environment.get(GradleEnvironment.GO_PIPELINE_COUNTER_BASE);
        counter = "" + (Integer.parseInt(counter) + Integer.parseInt(base));
      }
      setVariable(Build.BUILDNUMBER, counter);
    }

    if (!environment.isSet(Build.BUILD_DIR)) {
      setVariable(Build.BUILD_DIR, new File(workingDir, "build").getAbsolutePath());
    }
    if (!environment.isSet(Build.TARGET_DIR)) {
      setVariable(Build.TARGET_DIR, new File(workingDir, "target").getAbsolutePath());
    }
    if (!environment.isSet(Build.ARTIFACTS_DIR)) {
      setVariable(Build.ARTIFACTS_DIR, new File(workingDir, "artifacts").getAbsolutePath());
    }
  }

  /**
   * Parses the working directory for a GIT repository to get the versioning information.
   *
   * @param workingDir
   */
  private void parseRepository(File workingDir) throws IOException {
    RepositoryBuilder builder = new RepositoryBuilder(workingDir);
    try (Repository repo = builder.enableMonitor().build()) {
      Revision revision = repo.getRevision(Version.NONE);

      setVariable(Build.GIT_HASH, revision.getHash());
      setVariable(Build.GIT_DATE, revision.getISOTime());
      setVariable(Build.GIT_VERSION, revision.toString("0.0.0"));

      if (isSet(Build.BUILDNUMBER)) {
        Version version = revision.build(get(Build.BUILDNUMBER));
        revision = revision.build(version);
      } else {
        setVariable(Build.BUILDNUMBER, revision.getBuild());
      }
      setVariable(Build.REVISION, revision.toString("0.0.0+0")); // Used by IPA,AAB & APK

      repo.catchAndThrow();
    }
  }

  /**
   * Parses the environment variables for Windows.
   *
   * @param config
   * @param env
   */
  private void parseWindows(GradleConfig config, Environment env) {
    if (OS.isWindows()) {
      if (!env.isSet(Build.MSVC_ROOT)) {
        setVariable(Build.MSVC_ROOT, config.msvcRoot);
      }
      if (!env.isSet(Build.MSVC_VERSION)) {
        setVariable(Build.MSVC_VERSION, config.msvcVersion);
      }
    }
  }

  /**
   * Parses the environment variables for Android.
   *
   * @param config
   * @param environment
   */
  private void parseAndroid(GradleConfig config, Environment environment) {
    if (OS.isLinux()) {
      if (environment.isSet(Build.ANDROID_SDK_ROOT) || (config.androidSdkRoot != null)) {
        // Variables for Android development
        if (!environment.isSet(Build.ANDROID_SDK_ROOT)) {
          setVariable(Build.ANDROID_SDK_ROOT, config.androidSdkRoot);
        }

        if (!environment.isSet(Build.ANDROID_NDK_ROOT)) {
          String androidSdkRoot = get(Build.ANDROID_SDK_ROOT);
          setVariable(Build.ANDROID_NDK_ROOT, String.format("%s/ndk/%s", androidSdkRoot, config.androidNdkVersion));
        }

        if (environment.isSet(Build.ANDROID_ABIS)) {
          if (environment.get(Build.ANDROID_ABIS).contains(" ")) {
            setVariable(Build.ANDROID_ABIS, String.join(",", environment.get(Build.ANDROID_ABIS).split(" ")));
          }
        } else if (config.androidAbis != null) {
          setVariable(Build.ANDROID_ABIS, String.join(",", config.androidAbis));
        }

        if (!environment.isSet(Build.ANDROID_KEYSTORE) && (config.getAndroid().keystore != null)) {
          setVariable(Build.ANDROID_KEYSTORE, config.getAndroid().keystore);
        }
        if (!environment.isSet(Build.ANDROID_KEYSTORE_ALIAS) && (config.getAndroid().alias != null)) {
          setVariable(Build.ANDROID_KEYSTORE_ALIAS, config.getAndroid().alias);
        }
        if (!environment.isSet(Build.ANDROID_ID) && (config.getAndroid().id != null)) {
          setVariable(Build.ANDROID_ID, config.getAndroid().id);
        }
        if (!environment.isSet(Build.ANDROID_MANIFEST) && (config.getAndroid().manifest != null)) {
          setVariable(Build.ANDROID_MANIFEST, config.getAndroid().manifest);
        }
      }
    }
  }

  /**
   * Parses the environment variables for iOS.
   *
   * @param config
   * @param environment
   */
  private void parseiOS(GradleConfig config, Environment environment) {
    if (OS.isMacOS()) {
      if (!environment.isSet(Build.IOS_EXPORT_ID) && (config.getIos().id != null)) {
        setVariable(Build.IOS_EXPORT_ID, config.getIos().id);
      }
      if (!environment.isSet(Build.IOS_EXPORT_TYPE) && (config.getIos().type != null)) {
        setVariable(Build.IOS_EXPORT_TYPE, config.getIos().type);
      }
      if (!environment.isSet(Build.IOS_EXPORT_TEAM) && (config.getIos().team != null)) {
        setVariable(Build.IOS_EXPORT_TEAM, config.getIos().team);
      }
      if (!environment.isSet(Build.IOS_EXPORT_PLIST) && (config.getIos().export != null)) {
        setVariable(Build.IOS_EXPORT_PLIST, config.getIos().export);
      }
      if (!environment.isSet(Build.IOS_UPLOAD_API) && (config.getIos().apiKey != null)) {
        setVariable(Build.IOS_UPLOAD_API, config.getIos().apiKey);
      }
      if (!environment.isSet(Build.IOS_UPLOAD_ISSUER) && (config.getIos().issuerId != null)) {
        setVariable(Build.IOS_UPLOAD_ISSUER, config.getIos().issuerId);
      }
    }
  }

  /**
   * Parses the environment variables for Qt.
   *
   * @param config
   * @param environment
   */
  private void parseQt(GradleConfig config, Environment environment) {
    String targetDir = get(Build.TARGET_DIR);

    // Parses the variables for Qt
    if (environment.isSet(Build.QT_ROOT) || (config.qtRoot != null)) {
      if (!environment.isSet(Build.QT_ROOT)) {
        setVariable(Build.QT_ROOT, config.qtRoot);
      }

      if (!environment.isSet(Build.QT_VERSION) && (config.qtVersion != null)) {
        setVariable(Build.QT_VERSION, config.qtVersion);
      }

      if (!environment.isSet(Build.QT_CONFIG) && (config.qtConfig != null)) {
        setVariable(Build.QT_CONFIG, String.join(",", config.qtConfig));
      }

      // TODO: Deprecated
      setVariable(Build.QT_BUILD, targetDir);

      if (config.qtAndroid == null) {
        String qt_version = get(Build.QT_VERSION);
        QtPlatform platform = OS.isWindows() ? QtPlatform.WINDOWS : QtPlatform.LINUX;
        try {
          String msvc_version =
              environment.isSet(Build.MSVC_VERSION) ? environment.get(Build.MSVC_VERSION) : get(Build.MSVC_VERSION);
          Path androiddeployqt = Paths.get(qt_version).resolve(platform.getArch(msvc_version));
          setVariable(Build.QT_ANDROID_DEPLOY, androiddeployqt.toString());
        } catch (NullPointerException e) {}
      } else {
        // TODO: Workaround for 'androiddeployqt' on Qt 6.2
        setVariable(Build.QT_ANDROID_DEPLOY, config.qtAndroid);
      }
    }
  }

  /**
   * Parses the working directory for a GIT repository to get the versioning information.
   *
   * @param variables
   * @param targetDir
   */
  private void parseRevisionFile(String targetDir) throws IOException {
    File revisionFile = new File(targetDir, "revision");
    if (revisionFile.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(revisionFile))) {
        Version version = Version.of(reader.readLine());
        if (isSet(Build.BUILDNUMBER)) {
          version = version.build(get(Build.BUILDNUMBER));
        }

        setVariable(Build.GIT_VERSION, version.toString("0.0.0"));
        setVariable(Build.BUILDNUMBER, version.getBuild());
        setVariable(Build.REVISION, version.toString("0.0.0+0")); // Used by IPA,AAB & APK

        String hash = reader.readLine();
        if (hash != null) {
          setVariable(Build.GIT_HASH, hash);
        }

        String date = reader.readLine();
        if (date != null) {
          setVariable(Build.GIT_DATE, date);
        }
      }
    }
  }

  /**
   * Parses the working directory for a GIT repository to get the versioning information.
   *
   * @param workingDir
   */
  private void parseBrandings(File workingDir) throws IOException {
    File buildProperties = new File(workingDir, "platform/build.properties");
    if (buildProperties.exists()) {
      Properties props = new Properties();
      try {
        props.load(new FileReader(buildProperties));
        setVariable(Build.PRODUCT_NAME, props.getProperty("name"));
        setVariable(Build.PRODUCT_FILE, props.getProperty("file"));
        setVariable(Build.IOS_EXPORT_ID, props.getProperty("ios.id"));
        setVariable(Build.ANDROID_ID, props.getProperty("android.id"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Parses the {@link GradleConfig} and the GIT repository on the working directory to provide more
   * variables to the environment.
   *
   * @param config
   * @param workingDir
   * @param environment
   */
  public static Environment parse(GradleConfig config, File workingDir, Environment environment) throws IOException {
    GradleEnvironment env = new GradleEnvironment(environment);

    // Creates the default environment variables().
    env.parseDefaults(workingDir, environment);
    env.parseRepository(workingDir);

    env.parseWindows(config, environment);
    env.parseAndroid(config, environment);
    env.parseiOS(config, environment);

    env.parseQt(config, environment);

    env.parseRevisionFile(env.get(Build.TARGET_DIR));
    env.parseBrandings(workingDir);

    return env.getVariables().isEmpty() ? environment : env;
  }
}
