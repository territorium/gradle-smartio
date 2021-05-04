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

package it.smartio.task.git;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.smartio.build.Build;
import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.gradle.ProjectBuild;
import it.smartio.task.property.PropertyTask;
import it.smartio.util.git.Repository;
import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;


/**
 * The {@link GitTaskVersion} implements an abstract {@link Task} for GIT repositories.
 */
public class GitTaskVersion extends GitTask {

  private final ProjectBuild mode;

  /**
   * Constructs an instance of {@link GitTaskVersion}.
   */
  public GitTaskVersion() {
    this(ProjectBuild.None);
  }

  /**
   * Constructs an instance of {@link GitTaskVersion}.
   *
   * @param mode
   */
  public GitTaskVersion(ProjectBuild mode) {
    this.mode = mode;
  }

  /**
   * Handles a request for the provided repository.
   *
   * @param request
   * @param context
   */
  @Override
  protected void handleRequest(Repository request, TaskContext context) throws IOException {
    Revision revision = request.getRevision(Version.NONE);

    Version version = Version.of(revision.getVersion());
    version = version.build(revision.getBuildNumber());

    switch (this.mode) {
      case Patch:
        version = Version.of(version.getMajor(), version.getMinor(), version.getPatch() + 1, version.getName(),
            version.getBuild());
        break;

      case Release:
        version = Version.of(version.getMajor(), version.getMinor(), 0, version.getName(), version.getBuild());
        break;

      default:
        break;
    }

    Map<String, String> map = new HashMap<>();
    map.put(Build.GIT_HASH, revision.getHash());
    map.put(Build.GIT_DATE, revision.getISOTime());
    map.put(Build.GIT_VERSION, version.toString("0.0.0"));
    map.put(Build.BUILDNUMBER, revision.getBuild());
    map.put(Build.REVISION, version.toString("0.0.0+0")); // Used by IPA,AAB & APK
    Environment env = context.getEnvironment().map(map);

    new PropertyTask().handle(context.wrap(env));
  }
}
