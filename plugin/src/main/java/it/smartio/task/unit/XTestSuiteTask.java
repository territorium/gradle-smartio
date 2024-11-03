/**
 *
 */

package it.smartio.task.unit;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskList;
import it.smartio.common.task.process.ProcessRequestBuilder;
import it.smartio.common.task.process.ProcessTask;
import it.smartio.util.env.OS;

/**
 * The {@link XTestSuiteTask} class implements a test suite for XUnit based test cases.
 */
public class XTestSuiteTask extends TaskList {

  private final Pattern    pattern;
  private final QtPlatform platform;

  /**
   * Creates an instance of {@link XTestSuiteTask}.
   *
   * @param pattern
   * @param platform
   */
  public XTestSuiteTask(String pattern, QtPlatform platform) {
    this.pattern = XTestSuiteTask.asPattern(pattern);
    this.platform = platform;
  }

  /**
   * Gets the {@link Pattern}.
   */
  protected final Pattern getPattern() {
    return this.pattern;
  }

  /**
   * Collects the list of unit tests.
   *
   * @param tasks
   * @param context
   */
  @Override
  protected final void collect(List<Task> tasks, TaskContext context) {
    File targetDir = new File(context.getEnvironment().get(Build.TARGET_DIR));
    Path targetPath = targetDir.toPath();
    File binDir = targetPath.resolve(this.platform.getSpec()).resolve("bin").toFile();
    for (File test : binDir.listFiles()) {
      Matcher matcher = getPattern().matcher(test.getName());
      if (matcher.find()) {
        tasks.add(new XUnit(matcher.group(1)));
      }
    }
  }

  /**
   * Converts a file pattern like '*Unit*' in a regular expression.
   *
   * @param pattern
   */
  private static Pattern asPattern(String pattern) {
    String regexp = "(" + pattern.replace("*", ".+") + ")";
    if (OS.isWindows()) {
      regexp += "\\.exe";
    }
    return Pattern.compile("^" + regexp + "$", Pattern.CASE_INSENSITIVE);

  }

  /**
   * Creates a QMake process.
   */
  public static class XUnit extends ProcessTask {

    private final String unittest;

    /**
     * @param unittest
     */
    public XUnit(String unittest) {
      super(false);
      this.unittest = unittest;
    }


    /**
     * Gets the unit test.
     */
    protected final String getUnittest() {
      return this.unittest;
    }

    /**
     * Get the QMake shell command.
     */
    @Override
    protected final ProcessRequestBuilder getShellBuilder(TaskContext context) {
      File buildDir = new File(context.getEnvironment().get(Build.BUILD_DIR));
      File targetDir = new File(context.getEnvironment().get(Build.TARGET_DIR));

      File qtRoot = new File(context.getEnvironment().get(Build.QT_ROOT));
      File qtHome = new File(qtRoot, context.getEnvironment().get(Build.QT_VERSION));

      XUnitTestBuilder builder = new XUnitTestBuilder(buildDir);
      builder.setQtHome(qtHome);
      builder.setTargetDir(targetDir);
      builder.setUnitTest(getUnittest());

      if (OS.isWindows()) {
        builder.setMsvcRoot(new File(context.getEnvironment().get(Build.MSVC_ROOT)));
        builder.setMsvcVersion(context.getEnvironment().get(Build.MSVC_VERSION));
      }
      return builder;
    }
  }
}