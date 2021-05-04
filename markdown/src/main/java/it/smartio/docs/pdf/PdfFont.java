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

package it.smartio.docs.pdf;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link PdfFont} class.
 */
public class PdfFont {

  private final String       name;
  private final List<Metric> metrics = new ArrayList<>();

  /**
   * Constructs an instance of {@link PdfFont}.
   * 
   * @param name
   */
  public PdfFont(String name) {
    this.name = name;
  }

  /**
   * Gets the font name.
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Gets the collection of metrics.
   */
  final Iterable<Metric> getMetrics() {
    return this.metrics;
  }

  /**
   * Sets the {@link URI} to the font.
   */
  public final void addMetric(URI uri, boolean bold, boolean italic) {
    this.metrics.add(new Metric(uri, bold, italic));
  }

  /**
   * The {@link Metric} class.
   */
  class Metric {

    public final URI     uri;
    public final boolean bold;
    public final boolean italic;

    /**
     * Constructs an instance of {@link Metric}.
     *
     * @param uri
     * @param bold
     * @param italic
     */
    private Metric(URI uri, boolean bold, boolean italic) {
      this.uri = uri;
      this.bold = bold;
      this.italic = italic;
    }
  }
}
