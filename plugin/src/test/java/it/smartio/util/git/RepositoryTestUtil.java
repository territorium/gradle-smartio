
package it.smartio.util.git;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.submodule.SubmoduleWalk;

import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;

/**
 * @see https://github.com/centic9/jgit-cookbook
 */
public abstract class RepositoryTestUtil {

  private RepositoryTestUtil() {}

  /**
   * Print informations about the repository.
   *
   * @param info
   */
  public static void printRepository(RepositoryVerbose info) throws GitAPIException, IOException {
    System.out.println("\nShow branches:");
    info.listBranches().forEach(r -> System.out.printf("Branch: %s\t%s\n", r.getName(), r.getObjectId().getName()));

    System.out.println("\nShow local tags:");
    info.listTags().forEach(r -> System.out.printf("Tag: %s\t%s\n", r.getName(), r.getObjectId().getName()));

    System.out.println("\nShow remote branches: ");
    info.listRemoteTags().forEach(r -> System.out.printf("Branch: %s\t%s\n", r.getName(), r.getObjectId().getName()));

    System.out.println("\nShow sub modules: ");
    info.forEach(m -> RepositoryTestUtil.printSubModule(m));
  }

  /**
   * Print informations about the repository.
   *
   * @param module
   */
  public static void printSubModule(SubmoduleWalk module) {
    try {
      System.out.printf("%s:\n  path:\t\t%s\n  remote:\t%s\n\n", module.getModuleName(), module.getModulesPath(),
          module.getRemoteUrl());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Print the version of the {@link Repository}.
   *
   * @param repo
   */
  public static void printRevision(Repository repo) throws IOException {
    RepositoryTestUtil.printRevision(repo.getRevision(Version.NONE));
  }

  /**
   * Print the version of the {@link Repository}.
   *
   * @param revision
   */
  public static void printRevision(Revision revision) {
    System.out.printf("Version:\n  date:\t\t%s\n  hash:\t\t%s\n  version:\t%s\n\n", revision.getISOTime(),
        revision.getHash(), revision);
  }
}
