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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import it.smartio.util.Builder;


/**
 * The {@link RepositoryBuilder} class.
 */
public class RepositoryBuilder implements Builder<Repository> {

  private final File location;


  private String            remote;
  private String            branch;
  private String            username;
  private String            password;

  private ProgressMonitor   monitor;
  private final Set<String> modules = new LinkedHashSet<>();

  /**
   * Constructs an instance of {@link RepositoryBuilder}.
   *
   * @param location
   */
  public RepositoryBuilder(File location) {
    this.location = location;
  }

  /**
   * Sets the remote {@link URI}.
   *
   * @param remote
   */
  public final RepositoryBuilder setRemote(String remote) {
    this.remote = remote;
    return this;
  }

  /**
   * Sets the branch {@link URI}.
   *
   * @param branch
   */
  public final RepositoryBuilder setBranch(String branch) {
    this.branch = branch;
    return this;
  }

  /**
   * Sets the credentials.
   *
   * @param username
   * @param password
   */
  public final RepositoryBuilder setCredentials(String username, String password) {
    this.username = username;
    this.password = password;
    return this;
  }

  /**
   * Adds a GIT sub modules.
   *
   * @param modules
   */
  public final RepositoryBuilder addSubModules(String... modules) {
    this.modules.addAll(Arrays.asList(modules));
    return this;
  }

  /**
   * Enables a {@link ProgressMonitor}.
   */
  public final RepositoryBuilder enableMonitor() {
    this.monitor = new TextProgressMonitor();
    return this;
  }

  /**
   * Return <code>true</code> if the locatione exists.
   */
  public final boolean isAvailable() {
    return this.location.exists();
  }

  /**
   * Get {@link CredentialsProvider}.
   */
  protected final CredentialsProvider getCredentials() {
    if ((this.username != null) && (this.password != null)) {
      return new UsernamePasswordCredentialsProvider(this.username, this.password);
    }
    return null;
  }

  /**
   * Creates a {@link CloneCommand}.
   *
   * @param location
   * @param remote
   * @param credentials
   */
  private CloneCommand createClone(File location, String remote, CredentialsProvider credentials) {
    CloneCommand command = Git.cloneRepository();
    command.setDirectory(location);
    command.setURI(remote.toString()).setCredentialsProvider(credentials);
    command.setTagOption(TagOpt.FETCH_TAGS);
    command.setCloneSubmodules(false);
    command.setProgressMonitor(this.monitor);
    return command;
  }

  /**
   * Creates the root {@link Git} repository. If not available, it will be checked out.
   *
   * @param credentials
   */
  private Git getRepository(CredentialsProvider credentials) throws GitAPIException, IOException {
    if (this.location.exists()) {
      FileRepositoryBuilder builder = new FileRepositoryBuilder();
      builder.findGitDir(this.location);
      return new Git(builder.build());
    }

    if ((this.remote == null) || (credentials == null)) {
      throw new IllegalArgumentException("Remote and credentials are required for a checkout");
    }

    CloneCommand command = createClone(this.location, this.remote, credentials);
    return command.setBranch(this.branch).call();
  }

  /**
   * Build the {@link Repository} from the configuration in the builder.
   */
  @Override
  public final Repository build() {
    CredentialsProvider credentials = getCredentials();
    try {
      Git git = getRepository(credentials);
      Repository root = new Repository(git, credentials);
      try (SubmoduleWalk walk = SubmoduleWalk.forIndex(git.getRepository())) {
        while (walk.next()) {
          if (!this.modules.contains(walk.getModulesPath())) {
            continue;
          }

          if (walk.getRepository() == null) {
            File localPath = new File(git.getRepository().getWorkTree(), walk.getPath());
            CloneCommand command = createClone(localPath, walk.getRemoteUrl(), credentials);
            try (Repository repo = new Repository(command.call(), walk.getObjectId(), root)) {
              RevCommit commit = repo.getCommit(walk.getObjectId());
              repo.branch(commit, this.branch);
            }
          }
        }
      } catch (ConfigInvalidException e) {
        throw new IllegalArgumentException(e);
      }
      return root;
    } catch (GitAPIException | IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
