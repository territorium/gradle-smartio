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

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * The {@link SVGTools} class.
 */
public abstract class SVGTools {

  /**
   * Constructs an instance of {@link SVGTools}.
   */
  private SVGTools() {}

  /**
   * Sets the background of the node.
   *
   * @param node
   * @param background
   */
  private static void setBackground(SVGNode node, String background) {
    double opacity = 1.0;
    if (background.length() == 9) {
      opacity = Integer.parseUnsignedInt(background.substring(7), 16) / 256;
      background = background.substring(0, 7);
    }
    node.setAttribute("style", "fill:" + background + ";fill-opacity:" + opacity + ";stroke-width:0");
  }

  /**
   * Create a rounded SVG icon..
   *
   * @param width
   * @param height
   * @param bgColor
   */
  public static SVGNode createEllipse(double width, double height, String bgColor) {
    SVGNode ellipse = new SVGNode("ellipse", "");
    ellipse.setAttribute("rx", "" + (width / 2));
    ellipse.setAttribute("ry", "" + (height / 2));
    ellipse.setAttribute("cx", "" + (width / 2));
    ellipse.setAttribute("cy", "" + (height / 2));
    SVGTools.setBackground(ellipse, bgColor);
    return ellipse;
  }

  /**
   * Create a rounded SVG icon..
   *
   * @param width
   * @param height
   * @param bgColor
   */
  public static SVGNode createRectangle(double width, double height, String bgColor) {
    SVGNode rect = new SVGNode("rect", "");
    rect.setAttribute("width", "" + width);
    rect.setAttribute("height", "" + height);
    SVGTools.setBackground(rect, bgColor);
    return rect;
  }

  /**
   * Create a rounded SVG icon..
   *
   * @param node
   * @param bgColor
   */
  public static SVGDocument createRounded(SVGNode node, String bgColor) {
    double width = -1;
    double height = -1;
    if(node.getAttribute("viewBox") != null) {
        String[] view = node.getAttribute("viewBox").split(" ");
        width = Double.parseDouble(view[2]) - Double.parseDouble(view[0]);
        height = Double.parseDouble(view[3]) - Double.parseDouble(view[1]);
    }
    if (node.getAttribute("width") != null) {
      width = Double.parseDouble(node.getAttribute("width"));
    }
    if (node.getAttribute("height") != null) {
      height = Double.parseDouble(node.getAttribute("height"));
    }

    SVGDocument document = new SVGDocument();
    document.setAttribute("id", "svg");
    document.setAttribute("version", "1.1");
    document.setAttribute("width", "" + width);
    document.setAttribute("height", "" + height);
    document.setAttribute("viewBox", String.format("0 0 %s %s", width, height));

    document.addNode(SVGTools.createEllipse(width, height, bgColor));
    node.getChildren().forEach(c -> document.addNode(c));

    return document;
  }

  /**
   * Create a rounded SVG icon..
   *
   * @param node
   * @param bgColor
   */
  public static SVGDocument createSquare(SVGNode node, String bgColor) {
    double width = -1;
    double height = -1;
    if(node.getAttribute("viewBox") != null) {
        String[] view = node.getAttribute("viewBox").split(" ");
        width = Double.parseDouble(view[2]) - Double.parseDouble(view[0]);
        height = Double.parseDouble(view[3]) - Double.parseDouble(view[1]);
    }
    if (node.getAttribute("width") != null) {
      width = Double.parseDouble(node.getAttribute("width"));
    }
    if (node.getAttribute("height") != null) {
      height = Double.parseDouble(node.getAttribute("height"));
    }
    double size = Math.max(width, height);
    double dx = (size - width) / 2;
    double dy = (size - height) / 2;

    SVGDocument document = new SVGDocument();
    document.setAttribute("id", "svg");
    document.setAttribute("version", "1.1");
    document.setAttribute("width", "" + width);
    document.setAttribute("height", "" + height);
    document.setAttribute("viewBox", String.format("0 0 %s %s", size, size));
    document.addNode(SVGTools.createRectangle(size, size, bgColor));

    SVGNode transform = new SVGNode("g", "");
    transform.setAttribute("transform", "translate(" + dx + " " + dy + ")");
    document.addNode(transform);

    node.getChildren().forEach(c -> transform.addNode(c));

    return document;
  }

  /**
   * Creates a new {@link SVGDocument} rendering the provided document in the center of the new
   * document.
   *
   * @param node
   * @param width
   * @param height
   */
  public static SVGDocument createLauncher(SVGNode node, int width, int height) {
    double logoWidth = -1;
    double logoHeight = -1;
    if(node.getAttribute("viewBox") != null) {
        String[] view = node.getAttribute("viewBox").split(" ");
        logoWidth = Double.parseDouble(view[2]) - Double.parseDouble(view[0]);
        logoHeight = Double.parseDouble(view[3]) - Double.parseDouble(view[1]);
    }
    if (node.getAttribute("width") != null) {
      logoWidth = Double.parseDouble(node.getAttribute("width"));
    }
    if (node.getAttribute("height") != null) {
      logoHeight = Double.parseDouble(node.getAttribute("height"));
    }

    SVGDocument document = new SVGDocument();
    document.setAttribute("id", "svg");
    document.setAttribute("version", "1.1");
    document.setAttribute("width", "" + width);
    document.setAttribute("height", "" + height);
    document.setAttribute("viewBox", String.format("0 0 %s %s", width, height));

    document.addNode(SVGTools.createRectangle(width, height, "#ffffff"));

    SVGNode g = new SVGNode("g", "");
    g.setAttribute("transform", "translate(100 0)");
    document.addNode(g);

    node.getChildren().forEach(c -> g.addNode(c));

    double sx = width / 2.0 / logoWidth;
    double sy = height / 2.0 / logoHeight;
    double scale = Math.min(sx, sy);
    double dx = (width - (logoWidth * scale)) / 2;
    double dy = (height - (logoHeight * scale)) / 2;
    g.setAttribute("transform", String.format("translate(%1$s %2$s) scale(%3$s %3$s)", dx, dy, scale));

    return document;
  }

  /**
   * Converts an SVG to an PNG.
   *
   * @param text
   * @param width
   * @param height
   */
  public static byte[] convertSVGToPNG(String text, float width, float height) throws TranscoderException, IOException {
    try (Reader reader = new StringReader(text)) {
      try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
        TranscoderInput input = new TranscoderInput(reader);
        TranscoderOutput output = new TranscoderOutput(bytes);

        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height);
        transcoder.transcode(input, output);

        bytes.flush();
        return bytes.toByteArray();
      }
    }
  }
}
