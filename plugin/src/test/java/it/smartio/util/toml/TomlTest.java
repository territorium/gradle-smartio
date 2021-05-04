
package it.smartio.util.toml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;


class TomlTest {

  @Test
  void test() throws Exception {
    String bytes = "# This is a TOML document\n" + "\n" + "title = \"TOML Example\"\n" + "\n" + "[owner]\n"
        + "name = \"Tom Preston-Werner\"\n" + "dob = 1979-05-27T07:32:00-08:00\n" + "\n" + "[database]\n"
        + "enabled = true\n" + "ports = [ 8000, 8001, 8002 ]\n" + "data = [ [\"delta\", \"phi\"], [3.14] ]\n"
        + "temp_targets = { cpu = 79.5, case = 72.0 }\n" + "\n" + "[servers]\n" + "\n" + "[servers.alpha]\n"
        + "ip = \"10.0.0.1\"\n" + "role = \"frontend\"\n" + "\n" + "[servers.beta]\n" + "ip = \"10.0.0.2\"\n"
        + "role = \"backend\"";

    Map<String, Object> map = Toml.read(new ByteArrayInputStream(bytes.getBytes()));
    map = Toml.read(new File("/data/smartIO/release2304/platform/jlink/target/smartIO/conf/server.properties"));

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Toml.write(map, stream);
    System.out.println(new String(stream.toByteArray()));
  }
}
