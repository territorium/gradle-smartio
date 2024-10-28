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

package it.smartio.util.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * The {@link RepositoryVerbose} class.
 */
class RepositoryVerbose {

  private final Git                 repository;
  private final CredentialsProvider credentials;

  /**
   * Constructs an instance of {@link RepositoryVerbose}.
   *
   * @param repository
   * @param credentials
   */
  public RepositoryVerbose(Git repository, CredentialsProvider credentials) {
    this.repository = repository;
    this.credentials = credentials;
  }

  /**
   * List all branches.
   */
  public final Collection<Ref> listBranches() throws GitAPIException {
    return this.repository.branchList().setListMode(ListMode.ALL).call();
  }

  /**
   * List all local tags.
   */
  public final Collection<Ref> listTags() throws GitAPIException {
    return this.repository.tagList().call();
  }

  /**
   * List all remote tags.
   */
  public final Collection<Ref> listRemoteTags() throws GitAPIException {
    return this.repository.lsRemote().setCredentialsProvider(this.credentials).setTags(true).call();
  }

  /**
   * Iterates of every sub-module.
   *
   * @param module
   */
  public final void forEach(Consumer<SubmoduleWalk> module) throws IOException {
    try (SubmoduleWalk generator = SubmoduleWalk.forIndex(this.repository.getRepository())) {
      while (generator.next()) {
        module.accept(generator);
      }
    }
  }
}
