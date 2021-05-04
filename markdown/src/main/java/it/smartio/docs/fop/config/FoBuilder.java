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

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import it.smartio.docs.fluid.FluidBuilder;
import it.smartio.docs.fop.Fo;
import it.smartio.docs.fop.nodes.FoNode;
import it.smartio.docs.fop.nodes.FoRoot;

/**
 * The {@link FoBuilder} class.
 */
public class FoBuilder extends FluidBuilder {

  private final Map<String, UIPage>         templates = new LinkedHashMap<>();
  private final Map<String, UIPageSequence> pageSets  = new LinkedHashMap<>();

  /**
   * Constructs an instance of {@link FoBuilder}.
   *
   * @param workingDir
   */
  public FoBuilder(File workingDir) {
    super(workingDir);
  }

  /**
   * Get {@link UIPage} by name.
   *
   * @param name
   */
  public final UIPage getTemplate(String name) {
    return this.templates.get(name);
  }

  /**
   * Add a named {@link UIPage}.
   *
   * @param name
   */
  @Override
  public final UIPage addTemplate(String name) {
    UIPage page = new UIPage(name);
    this.templates.put(name, page);
    return page;
  }

  /**
   * Add a named {@link UIPage}.
   *
   * @param name
   */
  public final UIPageSequence addPageSet(String name) {
    this.pageSets.put(name, new UIPageSequence(name));
    return this.pageSets.get(name);
  }


  /**
   * Creates the {@link FoContext}.
   *
   * @param config
   */
  public FoContext build() {
    FoRoot root = FoNode.root(Fo.FONT_TEXT);
    root.set("xmlns:fox", "http://xmlgraphics.apache.org/fop/extensions");

    this.templates.values().stream().map(UIPage::getSimplePage).forEach(p -> root.getLayouts().addNode(p));
    this.pageSets.values().stream().map(UIPageSequence::getPageSet).forEach(s -> root.getLayouts().addNode(s));

    return new FoContext(root, getSymbols(), this.pageSets);
  }
}
