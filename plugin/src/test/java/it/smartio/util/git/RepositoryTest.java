
package it.smartio.util.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;


public class RepositoryTest {

  private final static File   LOCATION = new File("/data/build/test/smartio0");

  private final static String REMOTE   = "http://git.tol.info/smartio/smartio.git";
  private final static String USERNAME = "git";
  private final static String PASSWORD = "git";
  private final static String BRANCH   = "develop";

  /**
   * Create the default {@link RepositoryBuilder}.
   */
  protected RepositoryBuilder createBuilder() {
    RepositoryBuilder builder = new RepositoryBuilder(RepositoryTest.LOCATION);
    builder.setCredentials(RepositoryTest.USERNAME, RepositoryTest.PASSWORD);
    builder.setRemote(RepositoryTest.REMOTE).setBranch(RepositoryTest.BRANCH);
    builder.enableMonitor();
    return builder;
  }

  @Test
  public void testRevision() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("app");
    builder.addSubModules("v8-engine", "core-cpp", "torm", "spatial", "qzxing", "smartio4cpp", "app", "tormserver",
        "parser", "server", "web", "platform");

    try (Repository repo = builder.build()) {
      RepositoryTestUtil.printRevision(repo);
    }
  }

  // @Test
  public void testFetch() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("app");

    try (Repository repo = builder.build()) {
      repo.fetch();
    }
  }

  // @Test
  public void testPull() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("app");

    try (Repository repo = builder.build()) {
      repo.pull();
    }
  }

  // @Test
  public void testCheckout() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("app");

    try (Repository repo = builder.build()) {
      repo.checkout();
    }
  }

  // @Test
  public void testCommit() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("app");

    try (Repository repo = builder.build()) {
      repo.forEach(r -> r.commit("test"));
    }
  }

  // @Test
  public void testPatch() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("app");

    try (Repository repo = builder.build()) {
      repo.commit("test");
      // repo.push(true);

      repo.tag("version/x");
      // repo.push(false);
    }
  }

  // @Test
  public void testStash() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("app");

    try (Repository repo = builder.build()) {
      repo.forEach(r -> r.stash("Stash message"));
    }
  }

  // @Test
  public void testClient() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("v8-engine", "core-cpp", "torm", "spatial", "qzxing", "smartio4cpp", "app");

    try (Repository repo = builder.build()) {
      RepositoryTestUtil.printRepository(repo.getVerbose());
      RepositoryTestUtil.printRevision(repo);
    }
  }

  // @Test
  public void testServer() throws GitAPIException, IOException {
    RepositoryBuilder builder = createBuilder();
    builder.addSubModules("v8-engine", "core-cpp", "torm", "tormserver");

    try (Repository repo = builder.build()) {
      RepositoryTestUtil.printRepository(repo.getVerbose());

      repo.fetch();
      repo.checkout();
      repo.pull();
      repo.getExceptions().forEach(t -> t.printStackTrace());
    }
  }
}
