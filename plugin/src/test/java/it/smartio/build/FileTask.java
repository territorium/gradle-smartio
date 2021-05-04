
package it.smartio.build;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import it.smartio.common.env.Environment;
import it.smartio.gradle.pipeline.TestContext;
import it.smartio.task.file.CopyTask;


public class FileTask {

  @Test
  public void testCopyTask() throws IOException {
    TestContext context = new TestContext(new File(""), Environment.empty());

    String source = "/data/smartIO/projects/gmsh/smartIO/app";
    String target = "/data/smartIO/develop/app/platform";

    CopyTask task = new CopyTask(source, target);
    task.handle(context);
  }
}
