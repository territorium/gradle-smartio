/**
 *
 */

package it.smartio.task.xcode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import it.smartio.build.Build;
import it.smartio.build.QtPlatform;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.process.ProcessTask;
import it.smartio.util.file.FileMatcher;
import it.smartio.util.file.FilePattern;


/**
 * @author brigl
 *
 */
public class IPAUploadTask extends ProcessTask {

  private final String targetName;
  private final String moduleName;
  private final String artifact;


  public IPAUploadTask(String targetName, String moduleName, String artifact) {
    this.targetName = targetName;
    this.moduleName = moduleName;
    this.artifact = artifact;
  }

  @Override
  protected XCRunBuilder getShellBuilder(TaskContext context) {
    String msvc_version = context.getEnvironment().get(Build.MSVC_VERSION);
    File buildDir = new File(context.getEnvironment().get(Build.BUILD_DIR), this.moduleName);
    buildDir = new File(buildDir, QtPlatform.IOS.getArch(msvc_version));
    File ipa = new File(buildDir, String.format("%s.ipa", this.targetName));

    if (this.artifact != null) {
      File workingDir = context.getWorkingDir();
      String pattern = this.artifact;
      Path path = Paths.get(pattern);
      if (path.getNameCount() > 1) {
        workingDir = (path.isAbsolute() ? path.getParent() : workingDir.toPath().resolve(path.getParent())).toFile();
        pattern = path.getName(path.getNameCount() - 1).toString();
      }

      try {
        for (FileMatcher matcher : FilePattern.matches(workingDir, FilePattern.toRegExp(pattern))) {
          ipa = matcher.getFile();
          buildDir = workingDir;
        }
      } catch (IOException e) {}
    }

    XCRunBuilder builder = new XCRunBuilder(ipa, buildDir);
    builder.setApiKey(context.getEnvironment().get(Build.IOS_UPLOAD_API));
    builder.setIssuerId(context.getEnvironment().get(Build.IOS_UPLOAD_ISSUER));
    return builder;
  }
}
