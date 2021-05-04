/**
 *
 */

package it.smartio.util.archive;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Gzipped Tar archiver which preserves
 *
 * <ul> <li>POSIX file permissions</li> <li>Symbolic links (if the link target points inside the
 * archive)</li> <li>Last modification timestamp</li> </ul>
 *
 * in the archive as found in the filesystem for files to be archived. It uses GNU tar format
 * extensions for archive entries with path length > 100.
 */
class ArchiveZip extends Archive {

  /**
   * Creates a .tar.gz file
   *
   * @param file
   * @param name
   */
  public ArchiveZip(File file, String name) {
    super(file, name);
  }

  /**
   * Uncompress the provided ZIP-file to the target location
   *
   * @param target
   * @param folder
   */
  @Override
  public final LocalDateTime extractTo(File target, Path folder) throws IOException {
    LocalDateTime local = null;
    try (ZipInputStream stream = new ZipInputStream(getInputStream())) {
      ZipEntry entry = stream.getNextEntry();
      while (entry != null) {
        String entryName = relativize(entry.getName(), folder);
        if (entryName == null) {
          entry = stream.getNextEntry();
          continue;
        }

        LocalDateTime date = Instant.ofEpochMilli(entry.getTime()).atOffset(ZoneOffset.UTC).toLocalDateTime();
        if ((local == null) || date.isAfter(local)) {
          local = date;
        }

        File newFile = ArchiveUtil.newFile(target, entryName);
        if (entry.isDirectory()) {
          newFile.mkdirs();
        } else {
          newFile.getParentFile().mkdirs();
          ArchiveUtil.streamToFile(newFile, stream);
          newFile.setLastModified(entry.getTime());
        }
        stream.closeEntry();
        entry = stream.getNextEntry();
      }
    }
    return local;
  }

  /**
   * Archives the files.
   *
   * @param files
   */
  @Override
  public final ArchiveBuilder builder() throws IOException {
    getFile().getAbsoluteFile().getParentFile().mkdirs();
    return new ZipBuilder(new ZipOutputStream(getOutputStream(), Charset.defaultCharset()));
  }

  /**
   * The {@link TarBuilder} creates a builder to adding files to a TAR.
   */
  private class ZipBuilder extends ArchiveBuilder {

    /**
     * Constructs an instance of {@link TarBuilder}.
     */
    private ZipBuilder(ZipOutputStream stream) {
      super(stream);
    }

    /**
     * Gets the {@link OutputStream}.
     */
    @Override
    protected final ZipOutputStream getOutputStream() {
      return (ZipOutputStream) super.getOutputStream();
    }

    /**
     * Add the files matching the pattern to the {@link Archive}. Optional adds the path as prefix.
     *
     * @param directory
     * @param pattern
     * @param targetPath
     */
    @Override
    protected final void addToArchive(File directory, String pattern, String targetPath) throws IOException {
      for (File file : ArchiveTree.findFiles(directory, pattern)) {
        if (file.isFile()) {
          ZipEntry entry = ArchiveZip.createZipEntry(directory, file, targetPath);
          getOutputStream().putNextEntry(entry);
          ArchiveUtil.fileToStream(file, getOutputStream());
        }
      }
    }
  }


  /**
   * Create a {@link ZipEntry}.
   *
   * @param root
   * @param file
   */
  private static ZipEntry createZipEntry(File root, File file, String targetPath) throws IOException {
    return new ZipEntry(ArchiveUtil.relativePath(root, file, targetPath));
  }

}
