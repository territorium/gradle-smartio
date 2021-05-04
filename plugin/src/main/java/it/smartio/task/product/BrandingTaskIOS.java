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

package it.smartio.task.product;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import it.smartio.common.task.TaskContext;
import it.smartio.util.svg.SVGDocument;
import it.smartio.util.svg.SVGTools;


/**
 * The {@link BrandingTaskIOS} is used to create all Android & iOS branding details.
 */
public class BrandingTaskIOS extends BrandingTaskPlatform {


  @Override
  protected void render(File logo, File icon, String background, String bgColor, TaskContext context)
      throws IOException {
    Renderer renderer = null;

    if (logo.getName().endsWith(".png")) {
      BufferedImage pngLogo = ImageIO.read(logo);
      BufferedImage pngIcon = ImageIO.read(icon);
      // svgIcon = SVGTools.createSquare(svgIcon, bgColor);
      //
      renderer = new PngApple(pngIcon, pngLogo);
    } else {
      SVGDocument svgLogo = SVGDocument.parse(logo);
      SVGDocument svgIcon = SVGDocument.parse(icon);
      svgIcon = SVGTools.createSquare(svgIcon, bgColor);

      renderer = new SvgApple(svgIcon, svgLogo);
    }

    File target = context.getWorkingDir();
    renderer.render(new File(target, "ios"));
  }
}
