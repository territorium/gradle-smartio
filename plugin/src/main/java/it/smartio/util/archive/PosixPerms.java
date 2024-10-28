
package it.smartio.util.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * The POSIX file permission mask.
 */
class PosixPerms {

  private static final int OWNER_READ   = 0400;
  private static final int OWNER_WRITE  = 0200;
  private static final int OWNER_EXEC   = 0100;

  private static final int GROUP_READ   = 0040;
  private static final int GROUP_WRITE  = 0020;
  private static final int GROUP_EXEC   = 0010;

  private static final int OTHERS_READ  = 0004;
  private static final int OTHERS_WRITE = 0002;
  private static final int OTHERS_EXEC  = 0001;

  private PosixPerms() {}

  /**
   * Converts a set of {@link PosixFilePermission} to chmod-style octal file mode.
   */
  public static int toOctalFileMode(Set<PosixFilePermission> permissions) {
    int result = 0;
    for (PosixFilePermission permissionBit : permissions) {
      switch (permissionBit) {
        case OWNER_READ:
          result |= PosixPerms.OWNER_READ;
          break;
        case OWNER_WRITE:
          result |= PosixPerms.OWNER_WRITE;
          break;
        case OWNER_EXECUTE:
          result |= PosixPerms.OWNER_EXEC;
          break;
        case GROUP_READ:
          result |= PosixPerms.GROUP_READ;
          break;
        case GROUP_WRITE:
          result |= PosixPerms.GROUP_WRITE;
          break;
        case GROUP_EXECUTE:
          result |= PosixPerms.GROUP_EXEC;
          break;
        case OTHERS_READ:
          result |= PosixPerms.OTHERS_READ;
          break;
        case OTHERS_WRITE:
          result |= PosixPerms.OTHERS_WRITE;
          break;
        case OTHERS_EXECUTE:
          result |= PosixPerms.OTHERS_EXEC;
          break;
      }
    }
    return result;
  }

  public static boolean isExecuteable(int mode) {
    return ((mode & PosixPerms.OWNER_EXEC) > 0) || ((mode & PosixPerms.GROUP_EXEC) > 0)
        || ((mode & PosixPerms.OTHERS_EXEC) > 0);
  }

  /**
   * Get the {@link PosixFileAttributes} from the {@link File}.
   *
   * @param file
   */
  public static PosixFileAttributes getAttributes(File file) {
    PosixFileAttributeView view =
        Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);

    try {
      return (view == null) ? null : view.readAttributes();
    } catch (IOException e) {}
    return null;
  }
}