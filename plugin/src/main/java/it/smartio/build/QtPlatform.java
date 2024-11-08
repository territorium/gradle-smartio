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

package it.smartio.build;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.smartio.common.env.Environment;
import it.smartio.util.env.OS;

/**
 * The {@link QtPlatform} defines the available operating systems.
 */
public enum QtPlatform {

  IOS("ios", "macx-ios-clang"),
  LINUX("gcc_64", "linux-g++"),
  WINDOWS("msvcXXXX_64", "win32-msvc"),

  ANDROID("android", "android-clang"),
  ANDROID_ARM64_V8A("android", "android-clang", "arm64-v8a", "android_arm64_v8a"),
  ANDROID_ARMV7("android", "android-clang", "armeabi-v7a", "android_armv7"),
  ANDROID_X86("android", "android-clang", "x86", "android_x86"),
  ANDROID_X86_64("android", "android-clang", "x86_64", "android_x86_64");


  private final String arch;
  private final String spec;

  private final String abi;
  private final String abiPath;


  public final Set<String> ABIs = new HashSet<>();


  private QtPlatform(String arch, String spec) {
    this(arch, spec, null, arch);
  }

  private QtPlatform(String arch, String spec, String abi, String abiPath) {
    this.arch = arch;
    this.spec = spec;
    this.abi = abi;
    this.abiPath = abiPath;
  }

  public final String getArch(String version) {
    return OS.isWindows() ? this.arch.replace("XXXX", version) : this.arch;
  }

  public final String getSpec() {
    return this.spec;
  }

  public final String getABI() {
    return this.abi;
  }

  public final String getAbiPath(String version) {
    return OS.isWindows() ? this.abiPath.replace("XXXX", version) : this.abiPath;
  }

  public final boolean isAndroid() {
    return (this == ANDROID) || (this == ANDROID_ARMV7) || (this == ANDROID_ARM64_V8A) || (this == ANDROID_X86)
        || (this == ANDROID_X86_64);
  }

  /**
   * Get the Qt Path.
   *
   * @param workingDir
   */
  public final File toQtPath(File workingDir, String msvcVersion) {
    switch (this) {
      case ANDROID_X86:
      case ANDROID_X86_64:
      case ANDROID_ARM64_V8A:
      case ANDROID_ARMV7:
        return new File(workingDir, this.abiPath);

      case ANDROID:
      default:
        return new File(workingDir, getArch(msvcVersion));
    }
  }

  /**
   * Get the default {@link QtPlatform} for current operation system.
   */
  public static QtPlatform current() {
    switch (OS.current()) {
      case WINDOWS:
        return QtPlatform.WINDOWS;
      case MACOS:
        return QtPlatform.IOS;
      case LINUX:
      default:
        return QtPlatform.LINUX;
    }
  }

  /**
   * Gets the collections of available platforms for Qt.
   *
   * @param environment
   */
  public static Set<QtPlatform> getPlatforms(Environment environment) {
    File qtRoot = new File(environment.get(Build.QT_ROOT));
    File qtHome = new File(qtRoot, environment.get(Build.QT_VERSION));

    // Missing Qt architectures
    if (!qtHome.exists()) {
      return Collections.emptySet();
    }

    Set<QtPlatform> platforms = new HashSet<>();
    Set<String> declared = environment.isSet(Build.PLATFORM) ? Arrays.asList(environment.get(Build.PLATFORM).split(","))
        .stream().map(v -> v.toLowerCase()).collect(Collectors.toSet()) : Collections.emptySet();

    for (QtPlatform platform : QtPlatform.values()) {
      File qtArch = platform.toQtPath(qtHome, environment.get(Build.MSVC_VERSION));
      if (qtArch.exists()) {
        switch (platform) {
          case ANDROID:
            if ((declared.isEmpty() || declared.contains("android"))) {
              String abis = environment.get(Build.ANDROID_ABIS);
              if (abis.isEmpty()) {
                ANDROID.ABIs.add(ANDROID_ARMV7.abi);
                ANDROID.ABIs.add(ANDROID_ARM64_V8A.abi);
                ANDROID.ABIs.add(ANDROID_X86.abi);
                ANDROID.ABIs.add(ANDROID_X86_64.abi);
              } else {
                ANDROID.ABIs.addAll(Arrays.asList(abis.split(",")));
              }
              platforms.add(platform);
            }
            break;

          case ANDROID_ARMV7:
          case ANDROID_ARM64_V8A:
          case ANDROID_X86:
          case ANDROID_X86_64:
            if ((declared.isEmpty() || declared.contains("android"))) {
              String abis = environment.get(Build.ANDROID_ABIS);
              List<String> androidAbis = abis.isEmpty() ? Collections.emptyList() : Arrays.asList(abis.split(","));
              if (androidAbis.isEmpty() || androidAbis.contains(platform.abi)) {
                ANDROID.ABIs.add(platform.abi);
                platforms.add(platform);
              }
            }
            break;

          case IOS:
            if ((declared.isEmpty() || declared.contains("ios"))) {
              platforms.add(platform);
            }
            break;

          case LINUX:
            if ((declared.isEmpty() || declared.contains("linux"))) {
              platforms.add(platform);
            }
            break;

          case WINDOWS:
            if ((declared.isEmpty() || declared.contains("windows"))) {
              platforms.add(platform);
            }
            break;

          default:
        }
      }
    }

    return platforms;
  }

  /**
   * Gets the collections of available platforms for Qt.
   *
   * @param devices
   * @param environment
   */
  public static boolean isSupported(List<String> devices, Environment environment) {
    if (devices.isEmpty()) {
      return true;
    }

    Set<String> declared = environment.isSet(Build.PLATFORM) ? Arrays.asList(environment.get(Build.PLATFORM).split(","))
        .stream().map(v -> v.toLowerCase()).collect(Collectors.toSet()) : Collections.emptySet();

    switch (OS.current()) {
      case MACOS:
        return QtPlatform.hasSupport(devices, declared, "ios");
      case LINUX:
        return QtPlatform.hasSupport(devices, declared, "linux") || QtPlatform.hasSupport(devices, declared, "android");
      case WINDOWS:
        return QtPlatform.hasSupport(devices, declared, "windows");
    }
    return false;
  }


  private static boolean hasSupport(List<String> devices, Set<String> declared, String name) {
    return devices.contains(name) && (declared.isEmpty() || declared.contains(name));
  }
}
