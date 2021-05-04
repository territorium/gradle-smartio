
package it.smartio.build.task.repo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import it.smartio.build.task.AbstractTaskTest;
import it.smartio.common.env.Environment;
import it.smartio.task.repo.PackageTask;
import it.smartio.task.repo.RepositoryTask;


public class PackageTest extends AbstractTaskTest {

  public static final String SOURCE     = "packages2";
  public static final String ARTIFACTS  = "artifacts";

  public static final String PACKAGES   = "packages";
  public static final String REPOSITORY = "repository";

  public static final String QT_ROOT    = "/data/Software/Qt";
  public static final String BUILD_DIR  = "/tmp";
  public static final String TARGET_DIR = "/tmp";

  public static final String RELEASE    = "22.10-dev";

  @Test
  public void testPackageManual() throws GitAPIException, IOException {
    File workingDir = new File("/data/smartIO/installer");

    Map<String, String> map = new HashMap<>();
    Environment environment = Environment.empty().map(map);
    map.put("RELEASE", PackageTest.RELEASE);
    map.put("BUILD_DIR", PackageTest.BUILD_DIR);

    String root = "tol./smartio/$RELEASE/.";
    List<String> modules = Arrays.asList("help:(?<NAME>.+)-manual-(?<VERSION>.+).pdf => $RELEASE/$NAME-manual.pdf");
    PackageTask task = new PackageTask(PackageTest.SOURCE, PackageTest.PACKAGES, PackageTest.ARTIFACTS, root, modules);

    execute(task, workingDir, environment);
  }

  @Test
  public void testRepository() throws GitAPIException, IOException {
    File workingDir = new File("/data/smartIO/installer");

    Map<String, String> map = new HashMap<>();
    Environment environment = Environment.empty().map(map);
    map.put("QT_ROOT", PackageTest.QT_ROOT);
    map.put("BUILD_DIR", PackageTest.BUILD_DIR);
    map.put("TARGET_DIR", PackageTest.TARGET_DIR);

    RepositoryTask task = new RepositoryTask(PackageTest.PACKAGES, PackageTest.REPOSITORY);

    execute(task, workingDir, environment);
  }
}
