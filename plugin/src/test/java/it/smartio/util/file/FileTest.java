
package it.smartio.util.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;


public class FileTest {

  @Test
  public void testFilePattern() throws GitAPIException, IOException {
    File workingDir = new File("/home/brigl/Downloads/artifacts");
    String pattern = "../artifacts/smartIO-*.ipa";
    Path path = Paths.get(pattern);
    if (path.getNameCount() > 1) {
      workingDir = (path.isAbsolute() ? path.getParent() : workingDir.toPath().resolve(path.getParent())).toFile();
      pattern = path.getName(path.getNameCount() - 1).toString();
    }
    for (FileMatcher matcher : FilePattern.matches(workingDir, FilePattern.toRegExp(pattern))) {
      File file = matcher.getFile();
      System.out.println(file.getAbsolutePath());
    }
  }
}
