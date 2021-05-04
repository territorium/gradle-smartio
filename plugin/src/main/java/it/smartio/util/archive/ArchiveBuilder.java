/**
 *
 */

package it.smartio.util.archive;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Gzipped Tar archiver which preserves
 *
 * <pre>
 * <ul>
 *   <li>POSIX file permissions</li>
 *   <li>Symbolic links (if the link target points inside the archive)</li>
 *   <li>Last modification timestamp</li>
  </ul>
 * </pre>
 *
 * In the archive as found in the filesystem for files to be archived. It uses GNU tar format
 * extensions for archive entries with path length > 100.
 */

/**
 * The {@link ArchiveBuilder} implements a closable that allows to add new files to the
 * {@link ArchiveBuilder}.
 */
public abstract class ArchiveBuilder implements Closeable {

  private final OutputStream stream;

  /**
   * Constructs an instance of {@link ArchiveBuilder}.
   *
   * @param stream
   */
  protected ArchiveBuilder(OutputStream stream) {
    this.stream = stream;
  }

  /**
   * Gets the {@link OutputStream}.
   */
  protected OutputStream getOutputStream() {
    return this.stream;
  }

  /**
   * Add a file to the {@link Archive} using the directory.
   *
   * @param directory
   * @param pattern
   * @param targetPath
   */
  protected abstract void addToArchive(File directory, String pattern, String targetPath) throws IOException;

  /**
   * Add a file to the {@link ArchiveBuilder} using the directory.
   *
   * @param location
   * @param sourcePath
   * @param targetPath
   */
  public void addFile(File location, String sourcePath, String targetPath) throws IOException {
    addToArchive(location, sourcePath, targetPath);
  }

  /**
   * Add a file to the {@link ArchiveBuilder} using the directory.
   *
   * @param directory
   * @param file
   * @param targetPath
   */
  public final void addFile(File directory, File file, String targetPath) throws IOException {
    Path path = directory.toPath().relativize(file.toPath());
    addFile(directory, path.toString().replace('\\', '/'), targetPath);
  }

  /**
   * Archives the files.
   *
   * @param files
   * @param targetPath
   */
  public final void addFiles(List<File> files, String targetPath) throws IOException {
    for (File file : files) {
      addFile(file.getParentFile(), file, targetPath);
    }
  }

  /**
   * Archives the files in the directory
   *
   * @param directory
   */
  public final void addDirectory(File directory) throws IOException {
    for (File file : directory.listFiles()) {
      addFile(directory, file, null);
    }
  }

  /**
   * Closes this stream and releases any system resources associated with it.
   */
  @Override
  public final void close() throws IOException {
    this.stream.close();
  }
}