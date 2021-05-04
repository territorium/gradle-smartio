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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderException;

import it.smartio.util.svg.SVGDocument;
import it.smartio.util.svg.SVGTools;

/**
 * The {@link SvgAndroid} class.
 */
public class SvgAndroid implements Renderer {

  private final SVGDocument icon;
  private final SVGDocument logo;
  private final SVGDocument rounded;


  /**
   * Constructs an instance of {@link SvgAndroid}.
   *
   * @param icon
   * @param logo
   * @param rounded
   */
  public SvgAndroid(SVGDocument icon, SVGDocument logo, SVGDocument rounded) {
    this.icon = icon;
    this.logo = logo;
    this.rounded = rounded;
  }

  /**
   * Renders the files for a custom Android build.
   *
   * @param workingDir
   */
  @Override
  public void render(File workingDir) throws IOException {
    String contentIcon = this.icon.toString();
    String contentRounded = this.rounded.toString();

    for (Drawable drawable : Drawable.values()) {
      String name = drawable.name.isEmpty() ? "" : "-" + drawable.name;
      File folder = new File(workingDir, String.format("drawable%s", name));
      folder.mkdirs();

      // Create Icon
      try (OutputStream output = new FileOutputStream(new File(folder, "icon.png"))) {
        output.write(SVGTools.convertSVGToPNG(contentIcon, drawable.size, drawable.size));
      } catch (TranscoderException e) {
        throw new IOException(e);
      }

      // Create rounded Icon
      try (OutputStream output = new FileOutputStream(new File(folder, "round.png"))) {
        output.write(SVGTools.convertSVGToPNG(contentRounded, drawable.size, drawable.size));
      } catch (TranscoderException e) {
        throw new IOException(e);
      }

      String landscape = SVGTools.createLauncher(this.logo, drawable.width, drawable.height).toString();
      try (OutputStream output = new FileOutputStream(new File(folder, "landscape.png"))) {
        output.write(SVGTools.convertSVGToPNG(landscape, drawable.width, drawable.height));
      } catch (TranscoderException e) {
        throw new IOException(e);
      }

      String portrait = SVGTools.createLauncher(this.logo, drawable.height, drawable.width).toString();
      try (OutputStream output = new FileOutputStream(new File(folder, "screen.png"))) {
        output.write(SVGTools.convertSVGToPNG(portrait, drawable.height, drawable.width));
      } catch (TranscoderException e) {
        throw new IOException(e);
      }
    }
  }

  private enum Drawable {

    NONE("", 36f, 800, 480),
    LDPI("ldpi", 36f, 320, 200),
    MDPI("mdpi", 256f, 480, 320),
    HDPI("hdpi", 384f, 800, 480),
    XHDPI("xhdpi", 512f, 1280, 720),
    XXHDPI("xxhdpi", 768f, 1600, 960),
    XXXHDPI("xxxhdpi", 1024f, 1920, 1280);


    public final String name;
    public final float  size;
    public final int    width;
    public final int    height;

    /**
     * Constructs an instance of {@link AndroidDrawable}.
     *
     * @param name
     * @param size
     * @param width
     * @param height
     */
    private Drawable(String name, float size, int width, int height) {
      this.name = name;
      this.size = size;
      this.width = width;
      this.height = height;
    }
  }
}
