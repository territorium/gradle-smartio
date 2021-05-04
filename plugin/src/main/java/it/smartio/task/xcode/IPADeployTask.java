/**
 *
 */

package it.smartio.task.xcode;

import java.util.List;

import it.smartio.build.Build;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.TaskList;
import it.smartio.task.file.CopyTask;


/**
 * Defines a QMake task.
 */
public class IPADeployTask extends TaskList {

  private static final String SOURCE_FILE = "$" + Build.BUILD_DIR + "/%s/ios/%s.ipa";
  private static final String TARGET_FILE = "$" + Build.BUILD_DIR + "/%s-$" + Build.REVISION + ".ipa";


  private final String targetName;
  private final String moduleName;

  /**
   * Creates an instance of {@link IPADeployTask}.
   *
   * @param targetName
   * @param moduleName
   */
  public IPADeployTask(String targetName, String moduleName) {
    this.targetName = targetName;
    this.moduleName = moduleName;
  }

  @Override
  protected final void collect(List<Task> tasks, TaskContext context) {
    String sourceFilename = String.format(IPADeployTask.SOURCE_FILE, this.moduleName, this.targetName);
    String taregtFilename = String.format(IPADeployTask.TARGET_FILE, this.targetName);

    tasks.add(new XCExportTask(this.targetName, this.moduleName));
    tasks.add(new CopyTask(sourceFilename, taregtFilename));
  }
}