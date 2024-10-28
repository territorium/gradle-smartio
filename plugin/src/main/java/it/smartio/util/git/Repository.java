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

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.StashCreateCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.TagOpt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;

/**
 * The {@link Repository} provides a simple access to a GIT based repository, using the
 * https://github.com/centic9/jgit-cookbook. The {@link Repository} hides the implementation details
 * of the GIT repository and provides a simplified access.
 */
public class Repository implements AutoCloseable {

  private static final Pattern HASH = Pattern.compile("^[0-9a-fA-F]{40,40}$");


  private final Git                 git;
  private final AnyObjectId         oid;
  private final CredentialsProvider credentials;

  private final List<Throwable>     exceptions;

  /**
   * Constructs an instance of {@link Repository}.
   *
   * @param git
   * @param credentials
   */
  Repository(Git git, CredentialsProvider credentials) {
    this.git = git;
    this.oid = null;
    this.credentials = credentials;
    this.exceptions = new ArrayList<>();
  }

  /**
   * Constructs an instance of {@link Repository}.
   *
   * @param git
   * @param oid
   * @param parent
   */
  Repository(Git git, AnyObjectId oid, Repository parent) {
    this.git = git;
    this.oid = oid;
    this.credentials = parent.credentials;
    this.exceptions = parent.exceptions;
  }

  /**
   * Get the location of the {@link Repository}.
   */
  public final File getLocation() {
    return getGit().getRepository().getWorkTree();
  }

  /**
   * Get the {@link Git} handle.
   */
  protected final Git getGit() {
    return this.git;
  }

  /**
   * Get the {@link AnyObjectId} identifying the commit of the parent.
   */
  protected final AnyObjectId getObjectId() {
    return this.oid;
  }

  /**
   * Get the {@link CredentialsProvider}.
   */
  protected final CredentialsProvider getCredentials() {
    return this.credentials;
  }

  /**
   * Get exceptions for the current {@link Repository}.
   */
  public final List<Throwable> getExceptions() {
    List<Throwable> errors = new ArrayList<>(this.exceptions);
    this.exceptions.clear();
    return errors;
  }

  /**
   * Get exceptions for the current {@link Repository}.
   */
  public final Repository catchAndThrow() {
    List<Throwable> list = getExceptions();
    if (!list.isEmpty()) {
      throw new RuntimeException(list.stream().map(t -> t.getMessage()).collect(Collectors.joining("\n")));
    }
    return this;
  }

  /**
   * Add an {@link Exception}.
   *
   * @param exception
   */
  protected final void handleException(Exception exception) {
    this.exceptions.add(exception);
  }

  /**
   * Create {@link Repository} related information for verbose.
   */
  protected final RepositoryVerbose getVerbose() {
    return new RepositoryVerbose(this.git, this.credentials);
  }

  /**
   * Get the current branch.
   */
  public final String getBranch() throws IOException {
    return getGit().getRepository().getBranch();
  }

  /**
   * Get the latest {@link Repository} for a branch.
   *
   * @param version
   */
  public final Revision getRevision(Version version) throws IOException {
    return RepositoryVersion.getRevision(getGit(), version);
  }

  /**
   * Fetch all remote changes to the local repository.
   */
  public final void fetch() {
    forEach(r -> r.fetch());

    FetchCommand command = getGit().fetch();
    command.setCredentialsProvider(getCredentials());
    command.setTagOpt(TagOpt.FETCH_TAGS);
    command.setRemoveDeletedRefs(true);
    command.setCheckFetchedObjects(true);
    command.setRecurseSubmodules(FetchRecurseSubmodulesMode.YES);

    try {
      command.call();
    } catch (GitAPIException e) {
      handleException(e);
    }
  }

  /**
   * Pull all remote changes to the local repository.
   */
  public final void pull() {
    forEach(r -> r.pull());

    PullCommand command = getGit().pull();
    command.setCredentialsProvider(getCredentials());
    command.setFastForward(FastForwardMode.FF_ONLY);
    command.setContentMergeStrategy(ContentMergeStrategy.OURS);
    command.setRecurseSubmodules(FetchRecurseSubmodulesMode.YES);

    try {
      PullResult result = command.call();
      if (!result.isSuccessful()) {
        handleException(new RepositoryException("Pull aborted"));
      }
    } catch (GitAPIException e) {
      handleException(e);
    }
  }

  /**
   * Push all local changes to the remote repository.
   */
  public final void push() {
    forEach(r -> r.push());

    PushCommand command = getGit().push();
    command.setCredentialsProvider(getCredentials());
    command.setForce(true);

    try {
      command.call();
    } catch (GitAPIException e) {
      handleException(e);
    }

    command = getGit().push();
    command.setCredentialsProvider(getCredentials());
    command.setForce(true);
    command.setPushTags();

    try {
      command.call();
    } catch (GitAPIException e) {
      handleException(e);
    }
  }

  /**
   * Commit all changes with the provided message.
   *
   * @param message
   */
  public final void commit(String message) {
    forEach(r -> r.commit(message));

    CommitCommand command = getGit().commit();
    command.setCredentialsProvider(getCredentials());
    command.setMessage(message);
    command.setAll(true);

    try {
      if (!getGit().status().call().isClean()) {
        command.call();
      }
    } catch (GitAPIException e) {
      handleException(e);
    }
  }

  /**
   * Checkout a remote branch for all sub-modules.
   */
  public final void checkout() {
    forEach(r -> {
      try {
        r.stash(null);
        r.checkout(r.getCommit(r.getObjectId()));
      } catch (GitAPIException | IOException e) {
        handleException(e);
      }
    });
  }

  /**
   * Checkout a remote branch for all sub-modules.
   */
  public final void checkoutHard() {
    try {
      checkout(getBranch());
    } catch (GitAPIException | IOException e) {
      handleException(e);
    }
  }

  /**
   * Checkout a remote branch.
   *
   * @param name
   */
  public final RevCommit checkout(String name) throws GitAPIException, IOException {
    Ref ref = getGit().getRepository().findRef(name);

    CheckoutCommand command = getGit().checkout();
    command.setCreateBranch(ref == null);
    command.setStartPoint("origin/" + name).setName(name);
    ref = command.call();
    forEach(r -> {
      try {
        r.stash(null);
        r.checkout(r.getCommit(r.getObjectId()), name);
      } catch (GitAPIException | IOException e) {
        handleException(e);
      }
    });
    return ref == null ? null : getCommit(ref.getObjectId());
  }

  /**
   * Pull all remote changes to the local repository.
   */
  public final void tag(String tagName) {
    TagCommand command = getGit().tag();
    command.setName(tagName);
    command.setForceUpdate(true);

    try {
      command.call();
    } catch (GitAPIException e) {
      handleException(e);
    }
  }

  /**
   * Pull all remote changes to the local repository.
   */
  public final void stash(String message) {
    StashCreateCommand command = getGit().stashCreate();
    command.setIncludeUntracked(false);
    command.setIndexMessage(message);
    command.setWorkingDirectoryMessage(message);

    try {
      command.call();
    } catch (GitAPIException e) {
      handleException(e);
    }
  }

  /**
   * Get a commit form the hash, branch- or tag-name.
   *
   * @param name
   */
  public final RevCommit getCommit(String name) throws GitAPIException, IOException {
    Matcher matcher = Repository.HASH.matcher(name);
    if (matcher.find()) {
      return getCommit(ObjectId.fromString(name));
    }
    Ref ref = getGit().getRepository().findRef(name);
    return (ref == null) ? null : getCommit(ref.getObjectId());
  }

  /**
   * Get a commit form the {@link ObjectId}.
   *
   * @param id
   */
  protected final RevCommit getCommit(AnyObjectId id) throws GitAPIException, IOException {
    try (RevWalk walk = new RevWalk(getGit().getRepository())) {
      return walk.parseCommit(id);
    }
  }

  /**
   * Checkout a commit to the current local brach.
   *
   * @param commit
   */
  protected final void checkout(RevCommit commit) {
    try {
      String branch = getGit().getRepository().getBranch();
      checkout(commit, branch);
    } catch (GitAPIException | IOException e) {
      handleException(e);
    }
  }

  /**
   * Checkout a commit to the current local brach.
   *
   * @param commit
   */
  protected final void checkout(RevCommit commit, String branch) throws GitAPIException {
    String hash = commit.getId().getName();

    getGit().branchRename().setNewName(hash).setOldName(branch).call();
    getGit().branchDelete().setBranchNames(branch).setForce(true).call();
    getGit().checkout().setCreateBranch(true).setName(branch).setStartPoint(commit).call();
    getGit().branchDelete().setBranchNames(hash).setForce(true).call();
  }

  /**
   * Create a branch at commit and checkout.
   *
   * @param commit
   * @param branch
   */
  protected final void branch(RevCommit commit, String branch) throws GitAPIException, IOException {
    CreateBranchCommand command = getGit().branchCreate();
    command.setName(branch);
    command.setForce(true);
    command.setStartPoint(commit);
    command.call();
    checkout(commit, branch);
  }

  /**
   * Iterates of all {@link Repository}'s of the sub modules.
   *
   * @param consumer
   */
  public final void forEach(Consumer<Repository> consumer) {
    try (SubmoduleWalk walk = SubmoduleWalk.forIndex(getGit().getRepository())) {
      while (walk.next()) {
        if (walk.getRepository() != null) {
          Git git = new Git(walk.getRepository());
          try (Repository repo = new Repository(git, walk.getObjectId(), this)) {
            consumer.accept(repo);
          }
        }
      }
    } catch (IOException e) {
      handleException(e);
    }
  }

  /**
   * Closes the GIT {@link Repository}.
   */
  @Override
  public final void close() {
    this.exceptions.forEach(e -> e.printStackTrace());
    this.git.getRepository().close();
  }
}
