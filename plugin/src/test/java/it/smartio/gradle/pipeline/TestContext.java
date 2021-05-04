/**
 *
 */

package it.smartio.gradle.pipeline;

import java.io.File;
import java.io.InputStream;

import it.smartio.common.env.Environment;
import it.smartio.common.task.TaskContextAsync;
import it.smartio.common.task.TaskLogger;

/**
 * @author brigl
 *
 */
public class TestContext extends TaskContextAsync {

  private final TaskLogger logger = new TaskLogger() {

    @Override
    public void onRedirect(InputStream input, InputStream error) {
      TestContext.this.redirectStreams(input, error);
    }

    @Override
    public void onInfo(String message, Object... arguments) {
      System.out.println(String.format(message.replace("{}", "%s"), arguments));
    }

    @Override
    public void onError(Throwable throwable, String message, Object... arguments) {
      if (arguments.length == 0) {
        System.err.println(message);
      } else {
        System.err.println(String.format(message.replace("{}", "%s"), arguments));
      }
      throwable.printStackTrace();
    }
  };

  /**
   *
   * @param workingDir
   * @param environment
   */
  public TestContext(File workingDir, Environment environment) {
    super(workingDir, environment);
  }

  /**
   * Gets the {@link TaskLogger}.
   */
  @Override
  public final TaskLogger getLogger() {
    return this.logger;
  }
}
