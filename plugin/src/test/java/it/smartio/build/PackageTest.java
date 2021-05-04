
package it.smartio.build;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import it.smartio.common.env.Environment;
import it.smartio.task.repo.PackageBuilder;

/**
 * @see https://github.com/centic9/jgit-cookbook
 */
public class PackageTest {


  public static void main(String[] args) throws Exception {
    Map<String, String> map = new HashMap<>();
    Environment environment = Environment.empty().map(map);
    map.put("RELEASE", "22.02-dev");
    File workingDir = new File("/data/build/installer/packages2");

    PackageBuilder builder = new PackageBuilder(workingDir, environment);
    builder.setPackageDir(new File("//data/build/packages"));
    builder.setArtifactsDir(new File("/data/build/artifacts"));
    builder.addPackage("app.android", "smartIO-(?<VERSION>[0-9.\\-+]+)-armeabi-v7a.apk",
        "$RELEASE/webapps/client/smartio-$VERSION.apk");
    builder.build();
  }
}
