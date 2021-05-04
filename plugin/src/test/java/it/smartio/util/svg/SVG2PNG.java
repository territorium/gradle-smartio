/*
 * Copyright (c) 2001-2021 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.info/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package it.smartio.util.svg;

import java.io.File;
import java.io.IOException;

import org.apache.batik.transcoder.TranscoderException;

import it.smartio.task.product.SvgAndroid;
import it.smartio.task.product.SvgApple;

public class SVG2PNG {

  public static void main(String args[]) throws TranscoderException, IOException {
    File workingDir = new File("/data/smartIO/projects/m-net/smartIO/app");

    SVGDocument logo = SVGDocument.parse(new File(workingDir, "logo.svg"));
    SVGDocument icon = SVGDocument.parse(new File(workingDir, "icon.svg"));
    SVGDocument iconRect = SVGTools.createSquare(icon, "#ffffff");
    SVGDocument iconRound = SVGTools.createRounded(icon, "#ffffff");

    SvgAndroid android = new SvgAndroid(icon, logo, iconRound);
    android.render(new File(workingDir, "android/res"));

    SvgApple ios = new SvgApple(iconRect, logo);
    ios.render(new File(workingDir, "ios"));


    // File svg = new File("/home/brigl/Downloads", "logo.svg");
    // AndroidDrawable d = AndroidDrawable.NONE;
    // try (Reader reader = new FileReader(svg)) {
    // InputStream iStream = new ByteArrayInputStream(SVG2PNG.convertSVGToPNG(reader, 586, 200));
    // BufferedImage image = ImageIO.read(iStream);
    //
    // BufferedImage buffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB_PRE);
    // Graphics2D g = buffer.createGraphics();
    // g.drawRenderedImage(image, new AffineTransform());
    // ImageIO.write(buffer, "png", new File("/home/brigl/Downloads/test.png"));
    // }
  }
}