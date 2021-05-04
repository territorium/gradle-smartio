/**
 *
 */

package it.smartio.util.archive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Gzipped Tar archiver which preserves
 *
 * <pre>
 * - POSIX file permissions
 * -Symbolic links (if the link target points inside the archive)
 * -Last modification timestamp
 * </pre>
 *
 * In the archive as found in the filesystem for files to be archived. It uses GNU tar format
 * extensions for archive entries with path length greate than 100.
 */
public abstract class Archive {

  private final File   file;
  private final String name;

  /**
   * Creates a .tar.gz file
   *
   * @param file
   * @param name
   */
  public Archive(File file, String name) {
    this.file = file;
    this.name = name;
  }

  /**
   * Get the archive {@link File}.
   */
  protected final File getFile() {
    return this.file;
  }

  /**
   * Relativize the entry for the path. Returns <code>null</code> if the name doesn't match the
   * path.
   *
   * @param name
   * @param path
   */
  protected final String relativize(String name, Path path) {
    if (path == null) {
      return name;
    }

    Path entry = Paths.get(name);
    if (entry.equals(path) || !entry.startsWith(path)) {
      return null;
    }
    return path.relativize(entry).toString();
  }

  /**
   * Creates a specific {@link InputStream}.
   */
  protected InputStream getInputStream() throws IOException {
    return new BufferedInputStream(new FileInputStream(getFile()));
  }

  /**
   * Creates a specific {@link OutputStream}.
   */
  protected OutputStream getOutputStream() throws IOException {
    return new BufferedOutputStream(new FileOutputStream(getFile()));
  }

  /**
   * Extract the files in a directory with the name of the file
   */
  public final LocalDateTime extract() throws IOException {
    return extractTo(new File(getFile().getParentFile(), this.name), null);
  }

  /**
   * Extract to the target directory and return the last modification time.
   *
   * @param target
   */
  public final LocalDateTime extractTo(File target) throws IOException {
    return extractTo(target, null);
  }

  /**
   * Extract the path from to the target directory and return the last modification time.
   *
   * @param target
   * @param folder
   */
  public abstract LocalDateTime extractTo(File target, Path folder) throws IOException;

  /**
   * Creates an {@link ArchiveBuilder} to adding new files to the archive.
   */
  public abstract ArchiveBuilder builder() throws IOException;

  /**
   * Archive the files.
   *
   * @param file
   */
  public static Archive of(File file) throws IOException {
    String filename = file.getName();
    String name = filename.toLowerCase();

    if (name.endsWith(".tar")) {
      return new ArchiveTar(file, filename.substring(0, filename.length() - 4));
    } else if (name.endsWith(".tar.gz")) {
      return new ArchiveTarGz(file, filename.substring(0, filename.length() - 7));
    } else if (name.endsWith(".zip") || name.endsWith(".jar") || name.endsWith(".war")) {
      return new ArchiveZip(file, filename.substring(0, filename.length() - 4));
    }

    throw new UnsupportedEncodingException("Unsupported compression format for " + file.getName());
  }

  /**
   * Creates an {@link Archive} {@link ArchiveBuilder} for the file.
   *
   * @param file
   */
  public static ArchiveBuilder builder(File file) throws IOException {
    return Archive.of(file).builder();
  }
}