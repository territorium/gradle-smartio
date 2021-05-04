
package it.smartio.build.task.repo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import it.smartio.build.task.AbstractTaskTest;
import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;
import it.smartio.task.product.BrandingTask;


public class BrandingTest extends AbstractTaskTest {

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
    File workingDir = new File("/data/smartIO/release2304/app");

    Map<String, String> map = new HashMap<>();
    Environment environment = Environment.empty().map(map);
    // map.put("RELEASE", BrandingTest.RELEASE);
    // map.put("BUILD_DIR", BrandingTest.BUILD_DIR);

    Task task = new BrandingTask("/data/smartIO/projects/stadt-philippsburg/smartIO/app", "platform");
    execute(task, workingDir, environment);
  }
}
