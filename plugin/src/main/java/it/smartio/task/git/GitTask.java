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

import java.io.File;
import java.io.IOException;

import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.git.Repository;
import it.smartio.util.git.RepositoryBuilder;
import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;


/**
 * The {@link GitTask} implements an abstract {@link Task} for GIT repositories.
 */
public class GitTask implements Task {

  /**
   * A GIT Task that provides a {@link Repository}.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) {
    File workingDir = context.getWorkingDir();
    Environment environment = context.getEnvironment();

    RepositoryBuilder builder = new RepositoryBuilder(workingDir);
    builder.setBranch(environment.get(Git.BRANCH));

    if (environment.isSet(Git.REMOTE)) {
      builder.setRemote(environment.get(Git.REMOTE));
      if (environment.isSet(Git.USERNAME) && environment.isSet(Git.PASSWORD)) {
        builder.setCredentials(environment.get(Git.USERNAME), environment.get(Git.PASSWORD));
      }
    }

    if (environment.isSet(Git.MODULES)) {
      builder.addSubModules(environment.get(Git.MODULES).split(","));
    }

    try (Repository repo = builder.enableMonitor().build()) {
      handleRequest(repo, context);
      repo.catchAndThrow();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Handles a request for the provided repository.
   *
   * @param request
   * @param context
   */
  protected void handleRequest(Repository request, TaskContext context) throws IOException {
    Revision revision = request.getRevision(Version.NONE);

    String format = "GIT\n  " + String.join("\n  ", "WorkingDir:\t {}", "Branch:\t {}", "Hash:\t\t {}", "Date:\t\t {}",
        "Build:\t {}", "Version:\t {}");
    context.getLogger().onInfo(format, request.getLocation().getAbsolutePath(), request.getBranch(), revision.getHash(),
        revision.getISOTime(), revision.getBuildNumber(), revision.getVersion());

    if (context.getEnvironment().isSet(Git.MODULES)) {
      context.getLogger().onInfo("GIT Modules:\t {}", context.getEnvironment().get(Git.MODULES));
    }
  }
}
