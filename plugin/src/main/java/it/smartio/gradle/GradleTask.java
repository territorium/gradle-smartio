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

    logger.warn("WorkingDir: '{}'", workingDir);
    logger.warn("Loading environment variables...");
    try {
      environment = GradleEnvironment.parse(config, workingDir, environment);
      logger.warn(environment.toString());
    } catch (Throwable e) {
      logger.error("Couldn't load environment variables!", e);
      throw new RuntimeException(e);
    }
    logger.warn("Environment variables loaded!");

    GradleContext context = new GradleContext(logger, workingDir, environment);
    try {
      task.handle(context);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
