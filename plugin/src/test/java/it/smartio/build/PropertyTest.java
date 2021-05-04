
package it.smartio.build;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import it.smartio.common.env.Environment;
import it.smartio.common.task.TaskContext;
import it.smartio.gradle.ProjectBuild;
import it.smartio.gradle.pipeline.TestContext;
import it.smartio.task.git.Git;
import it.smartio.task.git.GitTaskVersion;


public class PropertyTest {

  private static final String BRANCH    = "develop";
  private static final String MODULES   = "deps-cpp";
  private static final String DIRECTORY = "/tmp/repo";

  private static final String USERNAME  = "git";
  private static final String PASSWORD  = "git";
  private static final String REMOTE    = "http://git.tol.info/smartio/smartio.git";


  private static TaskContext getContext(File workingDir) {
    Map<String, String> map = new HashMap<>();
    map.put(Git.LOCATION, PropertyTest.DIRECTORY);
    map.put(Git.REMOTE, PropertyTest.REMOTE);
    map.put(Git.USERNAME, PropertyTest.USERNAME);
    map.put(Git.PASSWORD, PropertyTest.PASSWORD);
    map.put(Git.BRANCH, PropertyTest.BRANCH);
    map.put(Git.MODULES, PropertyTest.MODULES);
    return new TestContext(workingDir, Environment.of(map));
  }

  @Test
  public void testProperty() throws IOException, GitAPIException {
    File workingDir = new File(PropertyTest.DIRECTORY);
    TaskContext context = PropertyTest.getContext(workingDir);

    GitTaskVersion task = new GitTaskVersion(ProjectBuild.Release);
    task.handle(context);
  }
}
