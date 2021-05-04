
package it.smartio.build.task;

import java.io.File;
import java.io.IOException;

import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;
import it.smartio.gradle.pipeline.TestContext;


public abstract class AbstractTaskTest {

  /**
   * Executes the task for the provided working directory and environment variables.
   *
   * @param task
   * @param workingDir
   * @param environment
   *
   */
  protected final void execute(Task task, File workingDir, Environment environment) throws IOException {
    task.handle(new TestContext(workingDir, environment));
  }
}
