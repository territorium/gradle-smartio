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

import org.apache.batik.transcoder.TranscoderException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import it.smartio.util.svg.SVGDocument;
import it.smartio.util.svg.SVGTools;

/**
 * The {@link SvgApple} class.
 */
public class SvgApple implements Renderer {

  private final SVGDocument icon;
  private final SVGDocument logo;


  /**
   * Constructs an instance of {@link SvgApple}.
   *
   * @param icon
   * @param logo
   */
  public SvgApple(SVGDocument icon, SVGDocument logo) {
    this.icon = icon;
    this.logo = logo;
  }

  /**
   * Renders the files for a custom iOS build.
   *
   * @param workingDir
   */
  @Override
  public void render(File workingDir) throws IOException {
    // Render LaunchImage's
    File launch = new File(workingDir, "Images.launch");
    launch.mkdirs();

    for (LaunchImage launcher : LaunchImage.values()) {
      File png = new File(launch, String.format("LaunchImage-iOS7%s.png", launcher.idiom));

      String content = SVGTools.createLauncher(this.logo, launcher.width, launcher.height).toString();
      try (OutputStream output = new FileOutputStream(png)) {
        output.write(SVGTools.convertSVGToPNG(content, launcher.width, launcher.height));
      } catch (TranscoderException e) {
        throw new IOException(e);
      }
    }


    // Render AppIcon's
    File appicon = new File(workingDir, "Images.xcassets/AppIcon.appiconset");
    appicon.mkdirs();

    createIconIOS(appicon);
  }


  private void createIconIOS(File folder) throws IOException {
    String content = this.icon.toString();

    JsonArrayBuilder images = Json.createArrayBuilder();
    for (IconSet e : IconSet.values()) {
      String name = String.format("AppIcon%s@%sx~%s.png", e.size, e.scale, e.idiom);
      File png = new File(folder, name);
      try (OutputStream output = new FileOutputStream(png)) {
        output.write(SVGTools.convertSVGToPNG(content, e.size * e.scale, e.size * e.scale));
      } catch (TranscoderException ex) {
        throw new IOException(ex);
      }


      JsonObjectBuilder image = Json.createObjectBuilder();
      image.add("size", "" + e.size + "x" + e.size);
      image.add("idiom", e.idiom);
      image.add("filename", name);
      image.add("scale", "" + e.scale + "x");
      images.add(image);
    }

    JsonObjectBuilder info = Json.createObjectBuilder();
    info.add("version", 1);
    info.add("author", "xcode");

    JsonObjectBuilder props = Json.createObjectBuilder();
    props.add("pre-rendered", true);

    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("images", images);
    builder.add("info", info);
    builder.add("properties", props);


    Map<String, Boolean> config = new HashMap<>();
    config.put(JsonGenerator.PRETTY_PRINTING, true);
    JsonWriterFactory factory = Json.createWriterFactory(config);

    File contents = new File(folder, "Contents.json");
    try (JsonWriter writer = factory.createWriter(new FileWriter(contents))) {
      writer.writeObject(builder.build());
    }
  }

  private enum LaunchImage {

    DEFAULT("-568h@2x", 640, 960),
    DEFAULT_2x("@2x", 640, 1136),
    PORTRAIT("-Portrait", 768, 1024),
    LANDSCAPE("-Landscape", 1024, 768),
    PORTRAIT_2x("-Portrait@2x", 1536, 2048),
    LANDSCAPE_2x("-Landscape@2x", 2048, 1536);


    public final String idiom;
    public final int    width;
    public final int    height;

    /**
     * Constructs an instance of {@link iOSIconSet}.
     *
     * @param idiom
     * @param width
     * @param height
     */
    private LaunchImage(String idiom, int width, int height) {
      this.idiom = idiom;
      this.width = width;
      this.height = height;
    }
  }

  private enum IconSet {

    IPHONE_20_2x("iphone", 20, 2),
    IPHONE_20_3x("iphone", 20, 3),
    IPHONE_29_2x("iphone", 29, 2),
    IPHONE_29_3x("iphone", 29, 3),
    IPHONE_40_2x("iphone", 40, 2),
    IPHONE_40_3x("iphone", 40, 3),
    IPHONE_60_2x("iphone", 60, 2),
    IPHONE_60_3x("iphone", 60, 3),

    IPAD_20_1x("ipad", 20, 1),
    IPAD_20_2x("ipad", 20, 2),
    IPAD_29_1x("ipad", 29, 1),
    IPAD_29_2x("ipad", 29, 2),
    IPAD_40_1x("ipad", 40, 1),
    IPAD_40_2x("ipad", 40, 2),
    IPAD_76_1x("ipad", 76, 1),
    IPAD_76_2x("ipad", 76, 2),
    IPAD_83_2x("ipad", 83.5f, 2),

    MARKETING("ios-marketing", 1024, 1);


    public final String idiom;
    public final float  size;
    public final int    scale;

    /**
     * Constructs an instance of {@link iOSIconSet}.
     *
     * @param idiom
     * @param size
     * @param scale
     */
    private IconSet(String idiom, float size, int scale) {
      this.idiom = idiom;
      this.size = size;
      this.scale = scale;
    }
  }
}
