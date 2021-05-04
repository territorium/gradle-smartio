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

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The {@link PngAndroid} class.
 */
public class PngAndroid implements Renderer {

  private final Image icon;
  private final Image logo;
  private final Image rounded;


  /**
   * Constructs an instance of {@link PngAndroid}.
   *
   * @param icon
   * @param logo
   * @param rounded
   */
  public PngAndroid(Image icon, Image logo, Image rounded) {
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
    for (Drawable drawable : Drawable.values()) {
      String name = drawable.name.isEmpty() ? "" : "-" + drawable.name;
      File folder = new File(workingDir, String.format("drawable%s", name));
      folder.mkdirs();

      // Create Icon
      try (OutputStream output = new FileOutputStream(new File(folder, "icon.png"))) {
        output.write(PNGTools.scalePNG(this.icon, drawable.size, drawable.size));
      }

      // Create rounded Icon
      try (OutputStream output = new FileOutputStream(new File(folder, "round.png"))) {
        output.write(PNGTools.scalePNG(this.rounded, drawable.size, drawable.size));
      }

      Image landscape = PNGTools.createLauncher(this.logo, drawable.width, drawable.height);
      try (OutputStream output = new FileOutputStream(new File(folder, "landscape.png"))) {
        output.write(PNGTools.scalePNG(landscape, drawable.width, drawable.height));
      }

      Image portrait = PNGTools.createLauncher(this.logo, drawable.height, drawable.width);
      try (OutputStream output = new FileOutputStream(new File(folder, "screen.png"))) {
        output.write(PNGTools.scalePNG(portrait, drawable.height, drawable.width));
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
