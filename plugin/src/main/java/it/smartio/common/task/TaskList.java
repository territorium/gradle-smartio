/**
 *
 */

package it.smartio.common.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link TaskList} implements a {@link Task} that can handle multiple sub-tasks.
 *
 */
public abstract class TaskList implements Task {

  /**
   * Handles a {@link Task} request.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) throws IOException {
    List<Task> tasks = new ArrayList<>();
    collect(tasks, context);
    for (Task task : tasks) {
      task.handle(context);
    }
  }

  /**
   * Collects the list of sub-tasks.
   *
   * @param tasks
   * @param context
   */
  protected abstract void collect(List<Task> tasks, TaskContext context);
}
