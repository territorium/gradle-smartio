/**
 *
 */

package it.smartio.task.repo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import it.smartio.build.Build;
import it.smartio.common.env.EnvironmentUtil;
import it.smartio.common.task.TaskContext;
import it.smartio.common.task.process.ProcessRequestBuilder;
import it.smartio.common.task.process.ProcessTask;

/**
 * Defines a QMake task.
 */
public class RepositoryTask extends ProcessTask {

  private final String packages;
  private final String repository;

  /**
   * Creates an instance of {@link RepositoryTask}.
   *
   * @param packages
   * @param repository
   */
  public RepositoryTask(String packages, String repository) {
    this.packages = packages;
    this.repository = repository;
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
   * Get the QMake shell command.
   */
  @Override
  protected final ProcessRequestBuilder getShellBuilder(TaskContext context) {
    Path packagesPath = toPath(this.packages, context);
    Path repositoryPath = toPath(this.repository, context);

    RepositoryBuilder builder = new RepositoryBuilder(packagesPath.toFile());
    builder.setQtRoot(new File(context.getEnvironment().get(Build.QT_ROOT)));
    builder.setRepository(repositoryPath.toFile());
    return builder;
  }
}