
package it.smartio.build;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.smartio.common.env.Environment;
import it.smartio.common.env.EnvironmentUtil;
import it.smartio.task.cpp.QMakeBuilder;
import it.smartio.util.archive.Assembly;
import it.smartio.util.git.Repository;
import it.smartio.util.git.RepositoryBuilder;
import it.smartio.util.version.Revision;
import it.smartio.util.version.Version;

public class PluginTest {

  private static final File WORKING_DIR = new File("D:/smartio/gradle-plugins/bin");

  @Test
  public void testRepository() throws Exception {
    RepositoryBuilder builder = new RepositoryBuilder(PluginTest.WORKING_DIR);
    builder.enableMonitor();

    try (Repository repo = builder.build()) {
      Revision version = repo.getRevision(Version.NONE);

      Assertions.assertEquals("41fda4317", version.getHash());
      Assertions.assertEquals("22.2", version.getRelease());
      Assertions.assertEquals("22.2.0", version.getVersion());
    }
  }

  @Test
  public void testQtProjects() throws Exception {
    for (File file : QMakeBuilder.findProjects(PluginTest.WORKING_DIR)) {
      System.out.println(file.getAbsolutePath());
    }
  }

  // @Test
  public void testArchiveTarGz() throws Exception {
    Map<String, String> map = new HashMap<>();
    Environment env = Environment.empty().map(map);
    map.put("WORKINGDIR", "bin");
    map.put("QT_HOME", "/data/Software/Qt/6.2.1/gcc_64");
    map.put("BUILDDIR", "bin/v8-engine/linux-g++");

    String files =
        "$WORKINGDIR/tormserver/conf/data.conf:conf;$WORKINGDIR/tormserver/smartIO-odb.sh:bin;$BUILDDIR/bin/smartIO-odb:bin;$BUILDDIR/lib/libsmartIO-{core,data,datasource}.so:lib;$QT_HOME/lib/libQt6{Core,Network,Sql}.so*:lib;$QT_HOME/lib/libicu{data,i18n,uc}.so*:lib;$QT_HOME/plugins/sqldrivers/libqsql{odbc,psql}.so:plugins/sqldrivers";
    String[] list = EnvironmentUtil.replace(files, env).split(";");

    File archive = new File(PluginTest.WORKING_DIR, EnvironmentUtil.replace("v8-engine/test.tar.gz", env));

    Assembly assembly = Assembly.of(PluginTest.WORKING_DIR.getParentFile());
    assembly.setArchive(archive);
    for (String pattern : list) {
      assembly.addPattern(pattern.trim());
    }
    assembly.build(m -> System.out.println(m));
  }

  @Test
  public void testArchiveZip() throws Exception {
    Map<String, String> map = new HashMap<>();
    Environment env = Environment.empty().map(map);
    map.put("WORKINGDIR", "bin");
    map.put("QT_HOME", "'D:/Software/Qt/6.2.1/msvc2019_64");
    map.put("BUILDDIR", "bin/v8-engine/win32-msvc");
    map.put("VERSION", "aaaa");
    map.put("BUILD", "BBB");


    String files =
        "$WORKINGDIR/tormserver/conf/data.conf:conf;$BUILDDIR/bin/smartIO-odb.exe:bin;$BUILDDIR/lib/smartIO-{core,data,datasource}.dll:bin,$BUILDDIR/lib/{v8,v8_libbase,v8_libplatform,zlib}.dll:bin;$QT_HOME/bin/Qt6{Core,Network,Sql}.dll:bin;$QT_HOME/plugins/sqldrivers/qsql{odbc,psql}.dll:bin/plugins/sqldrivers";
    String[] list = EnvironmentUtil.replace(files, env).split(";");

    File archive = new File(PluginTest.WORKING_DIR, EnvironmentUtil.replace("smartiodata-$VERSION+$BUILD.zip", env));

    Assembly assembly = Assembly.of(PluginTest.WORKING_DIR.getParentFile());
    assembly.setArchive(archive);
    for (String pattern : list) {
      assembly.addPattern(pattern.trim());
    }
    assembly.build(m -> System.out.println(m));
  }
}
