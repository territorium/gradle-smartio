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

import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.git.Repository;
import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;


/**
 * The {@link GitTaskTag} implements an abstract {@link Task} for GIT repositories.
 */
public class GitTaskTag extends GitTask {

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
    // Version version = Version.of(context.getEnvironment().get(Build.GIT_VERSION));
    // version = version.build(context.getEnvironment().get(Build.BUILDNUMBER));

    request.commit(String.format("Commit %s", version.toString("0.00.0+0")));
    request.tag(String.format("version/%s/%s", version.toString("0.00"), version.getPatch()));


    // if (this.push) {
    // repo.push();
    // }
  }
}
