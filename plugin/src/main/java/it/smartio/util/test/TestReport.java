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

package it.smartio.util.test;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * The {@link TestReport} class.
 */
public class TestReport implements Iterable<TestResult> {

  private final String           name;
  private final List<TestResult> results;
  private final Properties       properties;

  /**
   * Constructs an instance of {@link TestReport}.
   *
   * @param name
   * @param properties
   * @param result
   */
  public TestReport(String name, Properties properties, List<TestResult> result) {
    this.name = name;
    this.properties = properties;
    this.results = result;
  }

  /**
   * Gets the name of the {@link TestSuite}
   */
  public String getName() {
    return this.name;
  }

  public Properties getProperties() {
    return this.properties;
  }

  /**
   * Provides an iterator over the {@link TestCase}'s.
   */
  @Override
  public final Iterator<TestResult> iterator() {
    return this.results.iterator();
  }
}
