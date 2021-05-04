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

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;

import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;

/**
 * The {@link RepositoryVersion} class.
 */
abstract class RepositoryVersion {

  private static final Pattern PATTERN = Pattern.compile(
      "(?<major>\\d+)[./](?<minor>\\d+)(?:[./](?<patch>\\d+))?(?:-(?<name>[a-zA-Z0-9.]+))?(?:\\+(?<build>[a-zA-Z0-9.]+))?");


  /**
   * Constructs an instance of {@link RepositoryVersion}.
   */
  private RepositoryVersion() {}

  /**
   * Get the {@link OffsetDateTime} for the {@link RevCommit}.
   *
   * @param revCommit
   */
  public static OffsetDateTime getTime(RevCommit revCommit) {
    long instant = revCommit.getAuthorIdent().getWhen().getTime();
    return Instant.ofEpochMilli(instant).atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }

  /**
   * Count the number of commits {@link #getCount}.
   *
   * @param git
   */
  public static long getCommitCount(Git git) throws GitAPIException {
    return StreamSupport.stream(git.log().call().spliterator(), false).count();
  }

  /**
   * Get all reachable tags, ordered by version number.
   *
   * @param git
   * @param version
   */
  public static Revision getRevision(Git git, Version version) throws IOException {
    String branch = git.getRepository().getBranch();
    ObjectId refId = git.getRepository().resolve("HEAD");

    try (RevWalk walk = new RevWalk(git.getRepository())) {
      RevCommit revCommit = walk.parseCommit(refId);
      OffsetDateTime time = RepositoryVersion.getTime(revCommit);
      String hash = revCommit.getName().substring(0, 9);
      long buildNumber = RepositoryVersion.getCommitCount(git);

      if (Version.NONE.equals(version)) {
        Stream<TagInfo> stream = RepositoryVersion.getTags(git, revCommit, walk).stream();
        version = stream.map(tag -> tag.getVersion()).findFirst().orElse(Version.of(0, 0));
      }

      return new Revision(hash, time, version.build(buildNumber).preRelease(branch));
    } catch (GitAPIException e) {
      throw new IOException("Revision is not available on GIT", e);
    }
  }


  /**
   * Get all reachable tags, ordered by version number.
   *
   * @param git
   * @param rev
   * @param walk
   */
  private static Collection<TagInfo> getTags(Git git, RevCommit rev, RevWalk walk) throws GitAPIException {
    return git.tagList().call().stream().map(tag -> {
      try {
        RevCommit tagCommit = walk.parseCommit(tag.getObjectId());
        if (walk.isMergedInto(tagCommit, rev)) {
          Version version = Version.parse(tag.getName(), RepositoryVersion.PATTERN);
          int count = RevWalkUtils.count(walk, rev, tagCommit);
          return new TagInfo(tag, count, version);
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
      return new TagInfo(tag, -1, null);
    }).filter(i -> i.count != -1).sorted().collect(Collectors.toList());
  }


  /**
   * The {@link TagInfo} class.
   */
  public static class TagInfo implements Comparable<TagInfo> {

    private final Ref     ref;
    private final int     count;
    private final Version version;

    /**
     * Constructs an instance of {@link TagInfo}.
     *
     * @param ref
     */
    private TagInfo(Ref ref) {
      this(ref, -1, null);
    }

    /**
     * Constructs an instance of {@link TagInfo}.
     *
     * @param ref
     * @param count
     * @param version
     */
    private TagInfo(Ref ref, int count, Version version) {
      this.ref = ref;
      this.count = count;
      this.version = version;
    }

    /**
     * Gets the {@link #ref}.
     */
    public final Ref getRef() {
      return this.ref;
    }

    /**
     * Gets the {@link #ref}.
     */
    public final String getName() {
      return getRef().getName();
    }

    /**
     * Gets the {@link #version}.
     */
    public final Version getVersion() {
      return this.version;
    }

    @Override
    public int compareTo(TagInfo o) {
      return (this.count == o.count) ? this.version.compareTo(o.version) : Integer.compare(this.count, o.count);
    }
  }
}
