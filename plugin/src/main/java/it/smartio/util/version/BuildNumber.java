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

package it.smartio.util.version;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The {@link BuildNumber} class.
 */
public abstract class BuildNumber {

  private static final int            HOURS           = 1000 * 3600;
  private static final OffsetDateTime START_TIMESTAMP =
      OffsetDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.of(0, 0), ZoneOffset.ofHours(0));

  /**
   * Constructs an instance of {@link BuildNumber}.
   */
  private BuildNumber() {}

  /**
   * Get the next {@link BuildNumber}.
   */
  public static long next() {
    return (System.currentTimeMillis() - BuildNumber.START_TIMESTAMP.toInstant().toEpochMilli()) / BuildNumber.HOURS;
  }
}
