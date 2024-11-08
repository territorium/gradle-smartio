/**
 * 
 */

package it.smartio.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;

import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;

/**
 * 
 */
public abstract class GradleTask extends DefaultTask {

  protected GradleTask(String description) {
    setGroup("smart.io");
    setDescription(description);
  }

  protected void exec(Task task) {
    GradleConfig config = BuildPlugin.findConfig(getProject());

    File workingDir = config.getWorkingDir();
    Environment environment = Environment.system();
    Logger logger = config.getProject().getLogger();

    try {
      environment = GradleEnvironment.parse(config, workingDir, environment);
    } catch (Throwable e) {
      logger.error("Couldn't load environment variables!", e);
      throw new RuntimeException(e);
    } finally {
      logger.warn("WorkingDir: {}", workingDir);
      logger.warn("Environment variables:{}\n", environment.toString());
    }

    try {
      task.handle(new GradleContext(logger, workingDir, environment));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
