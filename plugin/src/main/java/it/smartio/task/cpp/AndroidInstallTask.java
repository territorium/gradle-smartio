/**
 *
 */

package it.smartio.task.cpp;

import java.io.File;
import java.util.List;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;


/**
 * Defines a QMake task.
 */
public class AndroidInstallTask extends MakeTask {

  /**
   * Creates an instance of {@link AndroidInstallTask}.
   *
   * @param platform
   * @param moduleName
   */
  public AndroidInstallTask(QtPlatform platform, String moduleName) {
    super(platform, moduleName, "install");
  }

  @Override
  protected void collect(List<Task> tasks, TaskContext context) {
    String msvc_version = context.getEnvironment().get(Build.MSVC_VERSION);
    String suffix = (this.platform.getABI() == null) ? "" : "-" + this.platform.getABI();
    File buildPath = new File(context.getEnvironment().get(Build.BUILD_DIR), this.moduleName);
    buildPath = new File(buildPath, this.platform.getArch(msvc_version) + suffix);
    tasks.add(new AndroidShellTask(buildPath, "android-build"));
  }

  /**
   * Creates a QMake process.
   */
  private class AndroidShellTask extends MakeShellTask {

    private final String installDir;

    /**
     * @param buildDir
     * @param installDir
     */
    public AndroidShellTask(File buildDir, String installDir) {
      super(buildDir);
      this.installDir = installDir;
    }

    /**
     * Get the QMake shell command.
     */
    @Override
    protected final CppBuilder getShellBuilder(TaskContext context) {
      MakeBuilder builder = (MakeBuilder) super.getShellBuilder(context);
      builder.setOption("INSTALL_ROOT", this.installDir);
      return builder;
    }
  }
}