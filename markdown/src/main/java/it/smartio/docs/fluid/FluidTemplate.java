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

import it.smartio.docs.fop.config.UIPageRegion;

/**
 * The {@link FluidTemplate} class.
 */
public interface FluidTemplate extends Fluid {

  /**
   * Set the template size.
   *
   * @param width
   * @param height
   */
  void setPageSize(String width, String height);

  /**
   * Set the columns of the template.
   *
   * @param count
   * @param gap
   */
  void setColumns(String count, String gap);

  /**
   * Add a {@link UIPageRegion}.
   *
   * @param name
   */
  public FluidRegion setRegion(String name, String position);

  /**
   * Get the padding of the template.
   *
   * @param count
   * @param gap
   */
  FluidMargin getPadding();
}
