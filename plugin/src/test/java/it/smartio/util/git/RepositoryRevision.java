
package it.smartio.util.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;


public class RepositoryRevision {

  private final static File   LOCATION     = new File("/data/smartIO/release2204");

  private final static String GIT_REMOTE   = "http://git.tol.info/smartio/smartio.git";
  private final static String GIT_USERNAME = "git";
  private final static String GIT_PASSWORD = "git";
  private final static String GIT_BRANCH   = "develop";

  /**
   * Create the default {@link RepositoryBuilder}.
   */
  protected RepositoryBuilder createBuilder() {
    RepositoryBuilder builder = new RepositoryBuilder(RepositoryRevision.LOCATION);
    builder.setCredentials(RepositoryRevision.GIT_USERNAME, RepositoryRevision.GIT_PASSWORD);
    builder.setRemote(RepositoryRevision.GIT_REMOTE).setBranch(RepositoryRevision.GIT_BRANCH);
    builder.enableMonitor();
    return builder;
  }

  @Test
  public void testRevision() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    // builder.addSubModules("app");
    // builder.addSubModules("v8-engine", "core-cpp", "torm", "spatial", "qzxing", "smartio4cpp",
    // "app", "tormserver",
    // "parser", "server", "web", "platform");

    try (Repository repo = builder.build()) {
      Revision revision = repo.getRevision(Version.NONE);
      RepositoryTestUtil.printRevision(revision);

      repo.forEach(r -> {
        System.out.println(r.getGit().getRepository().getDirectory());
        try {
          RepositoryTestUtil.printRevision(r.getRevision(revision));
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }
}
