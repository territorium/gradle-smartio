/*
 * Copyright (c) 2001-2024 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.docs.fop.config;

import java.net.URI;
import java.util.Stack;

import it.smartio.docs.fluid.FluidBuilder;
import it.smartio.docs.fluid.FluidParser;
import it.smartio.docs.fop.nodes.FoPageSequenceMaster.BlankOrNot;
import it.smartio.docs.fop.nodes.FoPageSequenceMaster.OddOrEven;
import it.smartio.docs.fop.nodes.FoPageSequenceMaster.Position;
import it.smartio.docs.util.DataUri;
import it.smartio.docs.util.StAX;
import it.smartio.docs.util.StAX.Attributes;

/**
 * The {@link FoContextHandler} class.
 */
class FoContextHandler extends FluidParser implements StAX.Handler {

  private final Stack<UIPageSequence> pageSets = new Stack<>();

  /**
   * Constructs an instance of {@link FoContextHandler}.
   *
   * @param builder
   */
  public FoContextHandler(FluidBuilder builder) {
    super(builder);
  }

  protected final FoBuilder getFoBuilder() {
    return (FoBuilder) getBuilder();
  }

  @Override
  public void handleEvent(String name, Attributes attrs) {
    switch (name) {
      case "template":
      case "formatter":
      case "symbols":
      case "symbol":
      case "page":

      case "font":
      case "font-metric":

      case "region":
        super.handleEvent(name, attrs);
        break;

      case "page-set":
        String id = attrs.get("name");
        pageSets.add(getFoBuilder().addPageSet(id));
        break;

      case "page-entry":
        UIPageSequence set = pageSets.peek();
        set.addPage(getFoBuilder().getTemplate(attrs.get("name")));
        if (attrs.isSet("blank")) {
          BlankOrNot blank = BlankOrNot.valueOf(attrs.get("blank"));
          set.getPageSet().addBlank(attrs.get("name"), blank);
        } else {
          Position position = Position.valueOf(attrs.get("position"));
          OddOrEven orientation = OddOrEven.valueOf(attrs.get("orientation"));
          set.getPageSet().addPage(attrs.get("name"), position, orientation);
        }
        break;

      case "panel":
        UIContainer container = getItem(UIContainer.class).addContainer();

        attrs.onAttribute("top", v -> container.setTop(v));
        attrs.onAttribute("left", v -> container.setLeft(v));
        attrs.onAttribute("right", v -> container.setRight(v));
        attrs.onAttribute("bottom", v -> container.setBottom(v));

        attrs.onAttribute("color", v -> container.setColor(v));
        attrs.onAttribute("background", v -> container.setBackground(v));

        attrs.onAttribute("font-size", v -> container.setFontSize(v));
        attrs.onAttribute("font-style", v -> container.setFontStyle(v));
        attrs.onAttribute("font-weight", v -> container.setFontWeight(v));
        attrs.onAttribute("text-align", v -> container.setTextAlign(v));;
        attrs.onAttribute("line-height", v -> container.setLineHeight(v));
        addItem(container);
        break;

      case "image":
        URI uri = DataUri.toURI(getBuilder().getWorkingDir(), attrs.get("uri"));
        UIImage image = new UIImage(uri);
        getItem(UIContainer.class).addItem(image);
        addItem(image);
        break;

      case "text":
        UIText text = new UIText();
        attrs.onAttribute("color", v -> text.setColor(v));
        attrs.onAttribute("font-size", v -> text.setFontSize(v));
        attrs.onAttribute("font-style", v -> text.setFontStyle(v));
        attrs.onAttribute("font-weight", v -> text.setFontWeight(v));
        attrs.onAttribute("text-align", v -> text.setTextAlign(v));;
        attrs.onAttribute("line-height", v -> text.setLineHeight(v));

        attrs.onAttribute("top", v -> text.setTop(v));
        attrs.onAttribute("left", v -> text.setLeft(v));
        attrs.onAttribute("right", v -> text.setRight(v));
        attrs.onAttribute("bottom", v -> text.setBottom(v));

        getItem(UIContainer.class).addItem(text);
        addItem(text);
        break;
    }
  }

  @Override
  public void handleEvent(String name, String content) {
    switch (name) {
      case "text":
        getItem(UIText.class).setText(content);

      default:
        super.handleEvent(name, content);
        break;
    }
  }
}
