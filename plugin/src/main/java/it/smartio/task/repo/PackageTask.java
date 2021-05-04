/**
 *
 */

package it.smartio.task.repo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.common.env.EnvironmentUtil;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;


/**
 * Defines a QMake task.
 */
public class PackageTask implements Task {

  private static final Pattern PATTERN = Pattern.compile("^([^:]+):([^=\\s]+)\\s*=>\\s*(.+)$");


  private final String       sources;
  private final String       packages;
  private final String       artifacts;

  private final String       modulePath;
  private final List<String> modules;

  /**
   * Creates an instance of {@link PackageTask}.
   *
   * @param sources
   * @param packages
   * @param artifacts
   */
  public PackageTask(String sources, String packages, String artifacts, String modulePath, List<String> modules) {
    this.sources = sources;
    this.packages = packages;
    this.artifacts = artifacts;
    this.modulePath = modulePath;
    this.modules = modules;
  }

  /**
   * Converts the path name to a {@link Path}, replacing environment variables.
   *
   * @param pathname
   * @param context
   */
  protected final Path toPath(String pathname, TaskContext context) {
    Path path = Paths.get(EnvironmentUtil.replace(pathname, context.getEnvironment()));
    return path.isAbsolute() ? path : context.getWorkingDir().toPath().resolve(path);
  }

  /**
   * Executes the {@link Task}.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) throws IOException {
    Path sourcesPath = toPath(this.sources, context);
    Path packagesPath = toPath(this.packages, context);
    Path artifactsPath = toPath(this.artifacts, context);

    PackageBuilder builder = new PackageBuilder(sourcesPath.toFile(), context.getEnvironment());
    builder.setPackageDir(packagesPath.toFile());
    builder.setArtifactsDir(artifactsPath.toFile());
    for (String module : this.modules) {
      Matcher matcher = PackageTask.PATTERN.matcher(module);
      if (matcher.find()) {
        String moduleName = this.modulePath + matcher.group(1);
        builder.addPackage(moduleName, matcher.group(2), matcher.group(3));
      }
    }
    builder.build();
  }
}