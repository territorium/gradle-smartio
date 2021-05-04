
package it.smartio.build.task.repo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import it.smartio.build.task.AbstractTaskTest;
import it.smartio.util.svg.SVGDocument;
import it.smartio.util.svg.SVGTools;


public class SVGTest extends AbstractTaskTest {

  @Test
  public void testPackageManual() throws GitAPIException, IOException {
    SVGDocument doc = SVGDocument.parse(new File("/data/smartIO/projects/gemeinde-nussloch/smartIO/app/icon.svg"));
    System.out.println(doc.toString());

    // Create Icon
    try (OutputStream output = new FileOutputStream(new File("/tmp", "icon.png"))) {
      output.write(SVGTools.convertSVGToPNG(doc.toString(), 400, 400));
    } catch (TranscoderException e) {
      throw new IOException(e);
    }
  }
}
