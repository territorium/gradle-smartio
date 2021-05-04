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

package it.smartio.docs.fop.nodes.set;

/**
 * The {@link FoMargin} class.
 */
public interface FoMargin<F extends FoMargin<?>> extends Fo {

  @SuppressWarnings("unchecked")
  default F setMargin(String value) {
    set("margin", value);
    return (F) this;
  }

  @SuppressWarnings("unchecked")
  default F setMarginTop(String value) {
    set("margin-top", value);
    return (F) this;
  }

  @SuppressWarnings("unchecked")
  default F setMarginLeft(String value) {
    set("margin-left", value);
    return (F) this;
  }

  @SuppressWarnings("unchecked")
  default F setMarginRight(String value) {
    set("margin-right", value);
    return (F) this;
  }

  @SuppressWarnings("unchecked")
  default F setMarginBottom(String value) {
    set("margin-bottom", value);
    return (F) this;
  }

  @SuppressWarnings("unchecked")
  default F setMargin(String left, String right, String top, String bottom) {
    setMarginTop(top);
    setMarginLeft(left);
    setMarginRight(right);
    setMarginBottom(bottom);
    return (F) this;
  }
}
