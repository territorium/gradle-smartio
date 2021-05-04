/**
 *
 */

package it.smartio.task.xcode;

import java.io.File;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.process.ProcessTask;
import it.smartio.task.xcode.XCodeBuilder.XCodeOperation;


/**
 * @author brigl
 *
 */
public class XCExportTask extends ProcessTask {

  private final String targetName;
  private final String moduleName;

  /**
   *
   */
  public XCExportTask(String targetName, String moduleName) {
    this.targetName = targetName;
    this.moduleName = moduleName;
  }

  @Override
  protected XCodeBuilder getShellBuilder(TaskContext context) {
    File buildDir = new File(context.getEnvironment().get(Build.BUILD_DIR), this.moduleName);
    buildDir = new File(buildDir, QtPlatform.IOS.arch);

    File exportPList = new File(context.getWorkingDir(), context.getEnvironment().get(Build.IOS_EXPORT_PLIST));
    XCodeBuilder builder = new XCodeBuilder(this.targetName, buildDir);
    builder.setXCodeOperation(XCodeOperation.EXPORT);
    builder.setIdentifier(context.getEnvironment().get(Build.IOS_EXPORT_ID));
    builder.setExportPList(exportPList);

    return builder;
  }
}
