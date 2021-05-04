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

/**
 * The {@link TestCase} class.
 */
public class TestCase {

  private final String name;
  private final Test   test;
  String               className;


  /**
   * Constructs an instance of {@link TestCase}.
   *
   * @param name
   */
  TestCase(String name, Test test) {
    this.name = name;
    this.test = test;
  }

  /**
   * Gets the test case name .
   */
  public final String getName() {
    return this.name;
  }

  public final TestResult execute() throws Throwable {
    TestResult result = new TestResult(this.name, this.className);
    long timestamp = System.currentTimeMillis();
    try {
      this.test.execute(result);
    } catch (Throwable e) {
      result.error = e;
    } finally {
      result.time = (System.currentTimeMillis() - timestamp) / 1000.0;
    }
    return result;
  }
}

