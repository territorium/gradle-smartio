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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * The {@link PNGTools} class.
 */
public abstract class PNGTools {

  /**
   * Constructs an instance of {@link PNGTools}.
   */
  private PNGTools() {}

  // /**
  // * Sets the background of the node.
  // *
  // * @param node
  // * @param background
  // */
  // private static void setBackground(SVGNode node, String background) {
  // double opacity = 1.0;
  // if (background.length() == 9) {
  // opacity = Integer.parseUnsignedInt(background.substring(7), 16) / 256;
  // background = background.substring(0, 7);
  // }
  // node.setAttribute("style", "fill:" + background + ";fill-opacity:" + opacity +
  // ";stroke-width:0");
  // }
  //
  // /**
  // * Create a rounded SVG icon..
  // *
  // * @param width
  // * @param height
  // * @param bgColor
  // */
  // public static Image createEllipse(double width, double height, String bgColor) {
  // SVGNode ellipse = new SVGNode("ellipse", "");
  // ellipse.setAttribute("rx", "" + (width / 2));
  // ellipse.setAttribute("ry", "" + (height / 2));
  // ellipse.setAttribute("cx", "" + (width / 2));
  // ellipse.setAttribute("cy", "" + (height / 2));
  // PNGTools.setBackground(ellipse, bgColor);
  // return ellipse;
  // }
  //
  // /**
  // * Create a rounded SVG icon..
  // *
  // * @param width
  // * @param height
  // * @param bgColor
  // */
  // public static SVGNode createRectangle(double width, double height, String bgColor) {
  // SVGNode rect = new SVGNode("rect", "");
  // rect.setAttribute("width", "" + width);
  // rect.setAttribute("height", "" + height);
  // PNGTools.setBackground(rect, bgColor);
  // return rect;
  // }
  //
  // /**
  // * Create a rounded SVG icon..
  // *
  // * @param node
  // * @param bgColor
  // */
  // public static SVGDocument createRounded(SVGNode node, String bgColor) {
  // String[] view = node.getAttribute("viewBox").split(" ");
  // double width = Double.parseDouble(view[2]) - Double.parseDouble(view[0]);
  // double height = Double.parseDouble(view[3]) - Double.parseDouble(view[1]);
  // if (node.getAttribute("width") != null) {
  // width = Double.parseDouble(node.getAttribute("width"));
  // }
  // if (node.getAttribute("height") != null) {
  // height = Double.parseDouble(node.getAttribute("height"));
  // }
  //
  // SVGDocument document = new SVGDocument();
  // document.setAttribute("id", "svg");
  // document.setAttribute("version", "1.1");
  // document.setAttribute("width", "" + width);
  // document.setAttribute("height", "" + height);
  // document.setAttribute("viewBox", String.format("0 0 %s %s", width, height));
  //
  // document.addNode(PNGTools.createEllipse(width, height, bgColor));
  // node.getChildren().forEach(c -> document.addNode(c));
  //
  // return document;
  // }
  //
  // /**
  // * Create a rounded SVG icon..
  // *
  // * @param node
  // * @param bgColor
  // */
  // public static SVGDocument createSquare(SVGNode node, String bgColor) {
  // String[] view = node.getAttribute("viewBox").split(" ");
  // double width = Double.parseDouble(view[2]) - Double.parseDouble(view[0]);
  // double height = Double.parseDouble(view[3]) - Double.parseDouble(view[1]);
  // if (node.getAttribute("width") != null) {
  // width = Double.parseDouble(node.getAttribute("width"));
  // }
  // if (node.getAttribute("height") != null) {
  // height = Double.parseDouble(node.getAttribute("height"));
  // }
  // double size = Math.max(width, height);
  // double dx = (size - width) / 2;
  // double dy = (size - height) / 2;
  //
  // SVGDocument document = new SVGDocument();
  // document.setAttribute("id", "svg");
  // document.setAttribute("version", "1.1");
  // document.setAttribute("width", "" + width);
  // document.setAttribute("height", "" + height);
  // document.setAttribute("viewBox", String.format("0 0 %s %s", size, size));
  // document.addNode(PNGTools.createRectangle(size, size, bgColor));
  //
  // SVGNode transform = new SVGNode("g", "");
  // transform.setAttribute("transform", "translate(" + dx + " " + dy + ")");
  // document.addNode(transform);
  //
  // node.getChildren().forEach(c -> transform.addNode(c));
  //
  // return document;
  // }

  /**
   * Creates a new {@link SVGDocument} rendering the provided document in the center of the new
   * document.
   *
   * @param image
   * @param width
   * @param height
   */
  public static Image createLauncher(Image image, int width, int height) {
    // String[] view = node.getAttribute("viewBox").split(" ");
    double logoWidth = image.getWidth(null);
    double logoHeight = image.getHeight(null);

    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics.setColor(Color.white);
    graphics.fillRect(0, 0, width, height);
    // document.addNode(PNGTools.createRectangle(width, height, "#ffffff"));

    double scale = Math.min(width / logoWidth, height / logoHeight) / 2.0;
    double dx = (width - (logoWidth * scale)) / 2;
    double dy = (height - (logoHeight * scale)) / 2;
    graphics.drawImage(image, (int) dx, (int) dy, (int) (logoWidth * scale), (int) (logoHeight * scale), null);

    return bufferedImage.getScaledInstance(width, height, height);
  }

  /**
   * Converts an SVG to an PNG.
   *
   * @param image
   * @param width
   * @param height
   */
  public static byte[] scalePNG(Image image, float width, float height) throws IOException {
    BufferedImage bufferedImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics.fillRect(0, 0, (int) width, (int) height);
    graphics.drawImage(image, 0, 0, (int) width, (int) height, null);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", stream);
    return stream.toByteArray();
  }
}
