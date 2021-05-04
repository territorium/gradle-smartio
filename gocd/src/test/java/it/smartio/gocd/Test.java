
package it.smartio.gocd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.gocd.util.Environment;

public class Test {

  private static final Pattern PATTERN_FILE = Pattern.compile("\\{([^}]+)\\}");
  private static final Pattern PATTERN_PATH = Pattern.compile("^([^\\{;]*)(?:/([^;]+))?(?:;(.+))?$");

  public static void main(String[] args) throws Exception {
    Environment env = new Environment();
    env.set("APP_VERSION", "1.0");
    env.set("BUILD_NUMBER", "123");
    env.set("QT_SPEC", "linux-g++");
    env.set("QT_ARCH", "gcc_64");
    env.set("QT_HOME", "/data/Software/Qt/5.15.2");

    Matcher matcher = PATTERN_PATH.matcher("lib/libsmartIO-{core,data}.so.{1,1.0.0};lib");
    if (matcher.find()) {
      System.out.println(">>>>>>>\n" + String.join("\n", matches2(matcher.group(2))));
    }
  }

  private static List<String> matches2(String text) {
    List<String> files = new ArrayList<>();
    Matcher m = PATTERN_FILE.matcher(text);

    int offset = 0;
    while (m.find()) {
      if (m.start() > offset) {
        append(files, text.substring(offset, m.start()));
      }
      append(files, m.group(1).split(","));
      offset = m.end();
    }
    if (offset < text.length()) {
      append(files, text.substring(offset));
    }
    return files;
  }

  private static void append(List<String> path, String... parts) {
    if (path.isEmpty()) {
      path.addAll(Arrays.asList(parts));
    } else {
      List<String> list = new ArrayList<>();
      for (String part : parts) {
        path.stream().forEach(p -> list.add(p + part));
      }
      path.clear();
      path.addAll(list);
    }
  }
}
