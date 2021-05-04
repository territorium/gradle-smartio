
package it.smartio.build;

import java.io.File;

import org.junit.jupiter.api.Test;

import it.smartio.common.env.Environment;
import it.smartio.common.env.EnvironmentUtil;
import it.smartio.util.archive.Archive;
import it.smartio.util.archive.ArchiveBuilder;
import it.smartio.util.archive.Assembly;

public class ArchiveTest {

  private final static File TARGET_DIR  = new File("/tmp");
  private final static File WORKING_DIR = new File("/home/brigl/build2104/linux-g++");

  @Test
  public void testTar() throws Exception {
    try (ArchiveBuilder c = Archive.builder(new File(ArchiveTest.TARGET_DIR, "test.tar"))) {
      c.addDirectory(new File(ArchiveTest.WORKING_DIR, "bin"));
      c.addFile(ArchiveTest.WORKING_DIR, "conf/proj.db", "");
    }
  }

  @Test
  public void testTarGz() throws Exception {
    try (ArchiveBuilder c = Archive.builder(new File(ArchiveTest.TARGET_DIR, "test.tar.gz"))) {
      c.addDirectory(new File(ArchiveTest.WORKING_DIR, "bin"));
      c.addFile(ArchiveTest.WORKING_DIR, "conf/proj.db", "");
    }
  }

  @Test
  public void testZip() throws Exception {
    try (ArchiveBuilder c = Archive.builder(new File(ArchiveTest.TARGET_DIR, "test.zip"))) {
      c.addDirectory(new File(ArchiveTest.WORKING_DIR, "bin"));
      c.addFile(ArchiveTest.WORKING_DIR, "conf/proj.db", "");
    }
  }

  @Test
  public void testAssembly() throws Exception {
    Environment env = Environment.empty();
    String source = "bin/{*};bin\nconf/{proj}.db;conf";
    File archive = new File(ArchiveTest.TARGET_DIR, EnvironmentUtil.replace("test2.tar", env));

    Assembly assembly = Assembly.of(ArchiveTest.WORKING_DIR);
    assembly.setArchive(archive);
    for (String pattern : source.split("[\\n]")) {
      assembly.addPattern(pattern.trim());
    }
    assembly.build(m -> System.out.println(m));
  }
}
