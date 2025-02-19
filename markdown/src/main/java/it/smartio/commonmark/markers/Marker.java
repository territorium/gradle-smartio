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

package it.smartio.commonmark.markers;

import org.commonmark.node.CustomNode;

/**
 * A strikethrough node containing text and other inline nodes nodes as children.
 */
public class Marker extends CustomNode {

  public enum Decoration {
    Overline,
    Underline,
    Highlight,
    Strikethrough;
  }

  private final Decoration decoration;

  /**
   * Constructs an instance of {@link Marker}.
   *
   * @param decoration
   */
  protected Marker(Decoration decoration) {
    this.decoration = decoration;
  }

  /**
   * Gets the {@link Decoration}..
   */
  public final Decoration getDecoration() {
    return this.decoration;
  }
}
