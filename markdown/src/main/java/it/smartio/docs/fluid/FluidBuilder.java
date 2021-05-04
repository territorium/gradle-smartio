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

package it.smartio.docs.fluid;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import it.smartio.docs.pdf.PdfFont;

/**
 * The {@link FluidBuilder} class.
 */
public abstract class FluidBuilder {

  private final File workingDir;

  private String     width;
  private String     height;


  private final FluidSymbols         symbols  = new FluidSymbols();
  private final Map<String, PdfFont> fonts    = new LinkedHashMap<>();
  private final Set<String>          keywords = new LinkedHashSet<>();

  /**
   * Constructs an instance of {@link FluidBuilder}.
   *
   * @param workingDir
   */
  public FluidBuilder(File workingDir) {
    this.workingDir = workingDir;
  }

  /**
   * Gets the working directory.
   */
  public final File getWorkingDir() {
    return this.workingDir;
  }

  /**
   * Gets the default width.
   */
  public final String getWidth() {
    return this.width;
  }

  /**
   * Gets the default height.
   */
  public final String getHeight() {
    return this.height;
  }

  /**
   * Gets the keywords.
   */
  public final Set<String> getKeywords() {
    return this.keywords;
  }

  /**
   * Gets the {@link FluidSymbols}.
   */
  public final FluidSymbols getSymbols() {
    return this.symbols;
  }

  /**
   * Gets the {@link #fonts}.
   */
  protected final Map<String, PdfFont> getFonts() {
    return fonts;
  }

  /**
   * Sets the default size.
   *
   * @param width
   * @param height
   */
  public final FluidBuilder setSize(String width, String height) {
    this.width = width;
    this.height = height;
    return this;
  }

  /**
   * Add a named {@link FluidTemplate}.
   *
   * @param name
   */
  public abstract FluidTemplate addTemplate(String name);

  /**
   * Adds a font by name.
   *
   * @param name
   */
  public final PdfFont addFontName(String name) {
    PdfFont font = new PdfFont(name);
    this.fonts.put(name, font);
    return font;
  }

  public final void forEachFont(Consumer<PdfFont> consumer) {
    this.fonts.values().forEach(f -> consumer.accept(f));
  }
}
