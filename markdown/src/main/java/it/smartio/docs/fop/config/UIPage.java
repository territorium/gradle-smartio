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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import it.smartio.docs.fluid.FluidMargin;
import it.smartio.docs.fluid.FluidTemplate;
import it.smartio.docs.fop.Fo;
import it.smartio.docs.fop.nodes.FoPageSequence;
import it.smartio.docs.fop.nodes.FoRegion;
import it.smartio.docs.fop.nodes.FoSimplePageMaster;

/**
 * The {@link UIPage} defines a page of the template.
 */
class UIPage implements FluidTemplate {

  private final String             name;
  private final FoSimplePageMaster simple;
  private final FoRegion           content;
  private final List<UIPageRegion> regions = new ArrayList<>();

  /**
   * Constructs an instance of {@link UIPage}.
   *
   * @param name
   */
  public UIPage(String name) {
    this.name = name;
    this.simple = new FoSimplePageMaster(name);
    this.content = this.simple.setBodyRegion(Fo.PAGE_CONTENT);
  }

  /**
   * Gets the name.
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Gets the {@link FoSimplePageMaster}.
   */
  public final FoSimplePageMaster getSimplePage() {
    return this.simple;
  }

  /**
   * Set a custom page size.
   *
   * @param width
   * @param height
   */
  @Override
  public void setPageSize(String width, String height) {
    this.simple.setPageSize(width, height);
  }

  /**
   * Get the {@link UIPage} padding.
   */
  @Override
  public final FluidMargin getPadding() {
    return new FluidMargin() {

      @Override
      public void setTop(String value) {
        UIPage.this.content.setMarginTop(value);
      }

      @Override
      public void setRight(String value) {
        UIPage.this.content.setMarginRight(value);
      }

      @Override
      public void setLeft(String value) {
        UIPage.this.content.setMarginLeft(value);
      }

      @Override
      public void setBottom(String value) {
        UIPage.this.content.setMarginBottom(value);
      }
    };
  }

  /**
   * Set the columns of the page.
   *
   * @param count
   * @param gap
   */
  @Override
  public void setColumns(String count, String gap) {
    this.content.setColumns(count, gap);
  }

  /**
   * Add a {@link UIPageRegion}.
   *
   * @param name
   */
  @Override
  public UIPageRegion setRegion(String name, String position) {
    if (name == null) {
      name = String.format("region-%s-%s", position, getName());
    }

    UIPageRegion region = null;

    switch (position.toLowerCase()) {
      case "top":
        region = new UIPageRegion(name, this.simple.addRegionBefore(name));
        break;
      case "left":
        region = new UIPageRegion(name, this.simple.addRegionStart(name));
        break;
      case "right":
        region = new UIPageRegion(name, this.simple.addRegionEnd(name));
        break;
      case "bottom":
        region = new UIPageRegion(name, this.simple.addRegionAfter(name));
        break;
    }

    this.regions.add(region);
    return region;
  }

  /**
   * Renders the regions of the page.
   *
   * @param page
   * @param properties
   */
  public void render(FoPageSequence sequence, Properties properties) {
    this.regions.forEach(r -> r.render(sequence, properties));
  }
}
