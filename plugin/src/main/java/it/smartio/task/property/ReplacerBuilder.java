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

package it.smartio.task.property;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.smartio.build.Build;
import it.smartio.common.env.Environment;
import it.smartio.task.product.Branding;
import it.smartio.util.Builder;
import it.smartio.util.version.Version;

/**
 * The {@link ReplacerBuilder} implements a default set of {@link Replacer}'s.
 */
public class ReplacerBuilder implements Builder<Replacer[]> {

  public static final Replacer[] DEFAULTS =
      { new JavaProperties(), new JavaManifest(), new AndroidManifest(), new IOSPList(), new MavenPOM(),
          new MavenPOMManifest(), new QMake(), new Qml(), new VersionReplacer(), new TemplateReplacer("*.md") };

  /**
   * Builds an instance of an {@link Replacer}.
   */
  @Override
  public final Replacer[] build() {
    throw new UnsupportedOperationException();
  }


  /**
   * The {@link JavaProperties} class implements a Manifest replacer for Java.
   */
  static class JavaProperties extends Replacer {

    public JavaProperties() {
      super("version.properties", "((GIT_VERSION|GIT_BUILD)\\s+=\\s?)([^\\n]+)(\\n)");
    }

    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "GIT_VERSION":
          return environment.isSet(Build.GIT_VERSION)
              ? Version.of(environment.get(Build.GIT_VERSION)).toString("0.00.0")
              : value;
        case "GIT_BUILD":
          return environment.isSet(Build.BUILDNUMBER) ? environment.get(Build.BUILDNUMBER) : value;
      }
      return value;
    }
  }

  /**
   * The {@link JavaManifest} implements a property replacer for the Java MANIFEST.MF.
   */
  static class JavaManifest extends Replacer {

    public JavaManifest() {
      super("MANIFEST.MF", "((Implementation-Version):\\s?)([^\\n]+)(\\n)");
    }

    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "Implementation-Version":
          return environment.isSet(Build.GIT_VERSION)
              ? Version.of(environment.get(Build.GIT_VERSION)).toString("0.00.0")
              : value;
      }
      return value;
    }
  }

  /**
   * The {@link AndroidManifest} class implements a Manifest replacer for Android.
   *
   * <pre>
   *   Glt;manifest package="info.tol.fm" android:versionName="22.02.1" android:versionCode="636"&gt;
   * </pre>
   */
  static class AndroidManifest extends Replacer {

    public AndroidManifest() {
      super("Manifest.xml", "((package|android:versionName|android:versionCode)=\")([^\"]+)(\")");
    }

    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "package":
          return environment.isSet(Build.ANDROID_ID) ? environment.get(Build.ANDROID_ID) : value;
        case "android:versionName":
          return environment.isSet(Build.GIT_VERSION) ? Version.of(environment.get(Build.GIT_VERSION)).toString("0.0.0")
              : value;
        case "android:versionCode":
          return environment.isSet(Build.BUILDNUMBER) ? environment.get(Build.BUILDNUMBER) : value;
      }
      return value;
    }
  }
  /**
   * The {@link IOSPList} class implements a Manifest replacer for iOS.
   *
   * <pre>
   *      &lt;key&gt;CFBundleIdentifier&lt;/key&gt;
   *      &lt;string&gt;info.tol.fm&lt;/string&gt;
   *      &lt;key&gt;CFBundleShortVersionString&lt;/key&gt;
   *      &lt;string&gt;22.02&lt;/string&gt;
   *      &lt;key&gt;CFBundleVersion&lt;/key&gt;
   *      &lt;string&gt;22.02.0&lt;/string&gt;
   *
   *      &lt;dict&gt;
   *        &lt;key&gt;bundle-identifier&lt;/key&gt;
   *        &lt;string&gt;info.tol.fm&lt;/string&gt;
   *        &lt;key&gt;bundle-version&lt;/key&gt;
   *        &lt;string&gt;22.02.0&lt;/string&gt;
   *      &lt;/dict&gt;
   * </pre>
   */
  static class IOSPList extends Replacer {

    public IOSPList() {
      super("*.plist",
          "(<key>(CFBundleDisplayName|CFBundleIdentifier|CFBundleVersion|CFBundleShortVersionString|bundle-identifier|bundle-version)</key>[^<]+<string>)([^<]+)(</string>)");
    }

    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "CFBundleDisplayName":
          return environment.isSet(Branding.NAME) ? environment.get(Branding.NAME) : value;
        case "CFBundleIdentifier":
        case "bundle-identifier":
          return environment.isSet(Build.IOS_EXPORT_ID) ? environment.get(Build.IOS_EXPORT_ID) : value;
        case "CFBundleShortVersionString":
          return environment.isSet(Build.GIT_VERSION) ? Version.of(environment.get(Build.GIT_VERSION)).toString("0.0.0")
              : value;
        case "CFBundleVersion":
        case "bundle-version":
          if (environment.isSet(Build.GIT_VERSION)) {
            String result = Version.of(environment.get(Build.GIT_VERSION)).toString("0.0.0");
            if (environment.isSet(Build.BUILDNUMBER)) {
              result += "." + environment.get(Build.BUILDNUMBER);
            }
            return result;
          }
      }
      return value;
    }
  }

  /**
   * The {@link MavenPOM} class implements a pom.xml replacer for finalName and warName.
   */
  static class MavenPOM extends Replacer {

    public MavenPOM() {
      super("pom.xml", "(<(finalName|warName)>.+-)(\\d+\\.\\d+\\.\\d+(?:\\+[^<]+)?)(</finalName>|</warName>)");
    }

    // <Version-Number>${git.version}</Version-Number>
    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "warName":
        case "finalName":
          return environment.isSet(Build.GIT_VERSION)
              ? Version.of(environment.get(Build.GIT_VERSION)).toString("0.00.0") + "+"
                  + environment.get(Build.BUILDNUMBER)
              : value;
      }
      return value;
    }
  }
  /**
   * The {@link MavenPOMManifest} class implements a pom.xml replacer for finalName and warName.
   */
  static class MavenPOMManifest extends Replacer {

    public MavenPOMManifest() {
      super("pom.xml",
          "(<(Version-Number|Version-Id|Version-Date)>)([^<]+)(</(?:Version-Number|Version-Id|Version-Date)>)");
    }

    // <Version-Id>${git.commit.hash}</Version-Id>
    // <Version-Date>${git.commit.date}</Version-Date>
    // <Version-Number>${git.version}</Version-Number>
    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "Version-Id":
          return environment.isSet(Build.GIT_HASH) ? environment.get(Build.GIT_HASH) : value;
        case "Version-Date":
          return environment.isSet(Build.GIT_DATE) ? environment.get(Build.GIT_DATE) : value;
        case "Version-Number":
          return environment.isSet(Build.GIT_VERSION)
              ? Version.of(environment.get(Build.GIT_VERSION)).toString("0.00.0")
              : value;
      }
      return value;
    }
  }

  /**
   * The {@link QMake} class implements a property replacer for .pri/.pro files.
   *
   * <pre>
   *   BUILD_NUMBER = 636
   *   BUILD_VERSION = 22.2.1
   *   GIT_VERSION = 22.2.1
   * </pre>
   */
  static class QMake extends Replacer {

    private static List<String> DEFAULTS = Arrays.asList("BUILD_VERSION", "BUILD_NUMBER", "GIT_VERSION");


    private final Set<String> variables;

    public QMake() {
      this(new HashSet<>(QMake.DEFAULTS));
    }

    public QMake(Set<String> variables) {
      super("*.{pri,pro}", "((" + String.join("|", variables) + ")\\s+=\\s?)([^\\n]+)(\\n)");
      this.variables = variables;
    }

    /**
     * Get the value from the environment variable.
     *
     * @param name
     * @param value
     * @param environment
     */
    @Override
    public final String getValue(String name, String value, Environment environment) {
      return (this.variables.contains(name) && environment.isSet(name)) ? environment.get(name) : value;
    }
  }


  /**
   * The {@link Qml} class implements a Manifest replacer for Java.
   *
   * <pre>
   *   BUILD_NUMBER = 636
   *   GIT_VERSION = 22.02.1
   * </pre>
   */
  static class Qml extends Replacer {

    public Qml() {
      super("*.qml", "(\\s+(custom\\w+)\\s*\\:\\s*)(\"[^\"\\n]*\")\\s*(\\n)");
    }

    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "customServer":
          return environment.isSet(Build.PRODUCT_RESOURCE) ? "\"" + environment.get(Build.PRODUCT_RESOURCE) + "\""
              : value;
        case "customModel":
          return environment.isSet(Build.PRODUCT_MODEL) ? "\"" + environment.get(Build.PRODUCT_MODEL) + "\"" : value;
        case "customOnline":
          return environment.isSet(Build.PRODUCT_OFFLINE) ? "\"OFFLINE\"" : value;
      }
      return value;
    }
  }

  /**
   * The {@link VersionReplacer} implements a property replacer for version files.
   */
  static class VersionReplacer extends Replacer {

    public VersionReplacer() {
      super("version", "((Version-Id|Version-Date|Version-Number|Build-Number):\\s?)(.+)(\\n)");
    }

    @Override
    public final String getValue(String name, String value, Environment environment) {
      switch (name) {
        case "Version-Id":
          return environment.isSet(Build.GIT_HASH) ? environment.get(Build.GIT_HASH) : value;
        case "Version-Date":
          return environment.isSet(Build.GIT_DATE) ? environment.get(Build.GIT_DATE) : value;
        case "Version-Number":
          return environment.isSet(Build.GIT_VERSION)
              ? Version.of(environment.get(Build.GIT_VERSION)).toString("0.00.0")
              : value;
        case "Build-Number":
          return environment.isSet(Build.BUILDNUMBER) ? environment.get(Build.BUILDNUMBER) : value;
      }
      return value;
    }
  }

  /**
   * The {@link TemplateReplacer} implements a template replacer for text files.
   */
  static class TemplateReplacer extends Replacer {

    public TemplateReplacer(String file) {
      super(file, "(\\{\\{([^}]+)(?:;[^}]+)?\\}\\})");
    }

    @Override
    public final String getValue(String template, String value, Environment environment) {
      String name = template.contains(";") ? template.substring(0, template.indexOf(';')) : template;
      String format = template.contains(";") ? template.substring(template.indexOf(';') + 1) : null;
      switch (name) {
        case "VERSION":
          if (environment.isSet(Build.GIT_VERSION)) {
            Version version = Version.of(environment.get(Build.GIT_VERSION));
            return version.toString(format == null ? "0.00" : format);
          }
          return value;
      }
      return value;
    }
  }
}
