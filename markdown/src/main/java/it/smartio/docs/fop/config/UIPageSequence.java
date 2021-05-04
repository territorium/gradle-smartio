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

import it.smartio.docs.fluid.Fluid;
import it.smartio.docs.fop.nodes.FoPageSequence;
import it.smartio.docs.fop.nodes.FoPageSequenceMaster;

/**
 * The {@link UIPageSequence} defines a set of {@link UIPage}'s.
 */
class UIPageSequence implements Fluid {

  private final FoPageSequenceMaster master;
  private final List<UIPage>         pages = new ArrayList<>();

  /**
   * Constructs an instance of {@link UIPageSequence}.
   *
   * @param name
   */
  public UIPageSequence(String name) {
    this.master = new FoPageSequenceMaster(name);
  }

  /**
   * Gets the {@link FoPageSequenceMaster}.
   */
  public final FoPageSequenceMaster getPageSet() {
    return this.master;
  }

  /**
   * Adds a {@link UIPage}.
   *
   * @param page
   */
  public void addPage(UIPage page) {
    this.pages.add(page);
  }

  /**
   * Renders the {@link UIPage}'s.
   *
   * @param sequence
   * @param properties
   */
  public void render(FoPageSequence sequence, Properties properties) {
    this.pages.forEach(p -> p.render(sequence, properties));
  }
}
