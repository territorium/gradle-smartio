/**
 *
 */

package it.smartio.util.archive;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;

/**
 * Gzipped Tar archiver which preserves
 *
 * <ul> <li>POSIX file permissions</li> <li>Symbolic links (if the link target points inside the
 * archive)</li> <li>Last modification timestamp</li> </ul>
 *
 * in the archive as found in the filesystem for files to be archived. It uses GNU tar format
 * extensions for archive entries with path length > 100.
 */
class ArchiveTar extends Archive {

  /**
   * Creates a .tar.gz file
   *
   * @param file
   * @param name
   */
  public ArchiveTar(File file, String name) {
    super(file, name);
  }

  /**
   * Uncompress the provided TAR/GZ-file to the target location
   *
   * @param target
   * @param path
   */
  @Override
  public LocalDateTime extractTo(File target, Path folder) throws IOException {
    LocalDateTime local = null;
    Map<File, String> symLinks = new HashMap<>();
    try (TarArchiveInputStream stream = new TarArchiveInputStream(getInputStream())) {
      TarArchiveEntry entry = stream.getNextTarEntry();
      while (entry != null) {
        String entryName = relativize(entry.getName(), folder);
        if (entryName == null) {
          entry = stream.getNextTarEntry();
          continue;
        }

        long lastModified = entry.getLastModifiedDate().getTime();
        LocalDateTime date = Instant.ofEpochMilli(lastModified).atOffset(ZoneOffset.UTC).toLocalDateTime();
        if ((local == null) || date.isAfter(local)) {
          local = date;
        }

        File newFile = ArchiveUtil.newFile(target, entry.getName());
        if (entry.isDirectory()) {
          newFile.mkdirs();
        } else if (entry.isSymbolicLink()) {
          symLinks.put(newFile, entry.getLinkName());
        } else {
          newFile.getParentFile().mkdirs();
          ArchiveUtil.streamToFile(newFile, stream);
          newFile.setLastModified(entry.getLastModifiedDate().getTime());
          newFile.setExecutable(PosixPerms.isExecuteable(entry.getMode()));
        }
        entry = stream.getNextTarEntry();
      }

      for (File file : symLinks.keySet()) {
        Files.createSymbolicLink(file.toPath(), Paths.get(symLinks.get(file)));
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

    TarArchiveOutputStream stream = new TarArchiveOutputStream(getOutputStream(), "UTF-8");
    stream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    return new TarBuilder(stream);
  }

  /**
   * The {@link TarBuilder} creates a builder to adding files to a TAR.
   */
  private class TarBuilder extends ArchiveBuilder {

    /**
     * Constructs an instance of {@link TarBuilder}.
     *
     * @param stream
     */
    private TarBuilder(TarArchiveOutputStream stream) {
      super(stream);
    }

    /**
     * Gets the {@link OutputStream}.
     */
    @Override
    protected final TarArchiveOutputStream getOutputStream() {
      return (TarArchiveOutputStream) super.getOutputStream();
    }

    /**
     * Add a file to the {@link Archive} using the directory.
     *
     * @param directory
     * @param pattern
     * @param targetPath
     */
    @Override
    protected final void addToArchive(File directory, String pattern, String targetPath) throws IOException {
      for (File file : ArchiveTree.findFiles(directory, pattern)) {
        TarArchiveEntry entry = ArchiveTar.createTarEntry(directory, file, targetPath);
        PosixFileAttributes attributes = PosixPerms.getAttributes(file);
        if (attributes != null) {
          entry.setMode(PosixPerms.toOctalFileMode(attributes.permissions()));
        }
        entry.setModTime(file.lastModified());

        getOutputStream().putArchiveEntry(entry);
        if (file.isFile() && !entry.isSymbolicLink()) {
          ArchiveUtil.fileToStream(file, getOutputStream());
        }
        getOutputStream().closeArchiveEntry();
      }
    }
  }


  /**
   * Create a {@link TarArchiveEntry}.
   *
   * @param root
   * @param file
   */
  private static TarArchiveEntry createTarEntry(File root, File file, String targetPath) throws IOException {
    String path = ArchiveUtil.relativePath(root, file, targetPath);

    // only create symlink entry if link target is inside archive
    if (ArchiveUtil.isSymbolicLink(file) && ArchiveUtil.resolvesBelow(file, root)) {
      TarArchiveEntry entry = new TarArchiveEntry(path, TarConstants.LF_SYMLINK);
      entry.setLinkName(ArchiveUtil.slashify(ArchiveUtil.getRelativeSymLinkTarget(file, file.getParentFile())));
      return entry;
    }
    return new TarArchiveEntry(file, path);
  }
}
