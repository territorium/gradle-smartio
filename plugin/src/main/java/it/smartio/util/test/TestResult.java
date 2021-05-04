/*
 * Copyright (c) 2001-2022 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.bz.it/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package it.smartio.util.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import it.smartio.util.http.postman.JsonUtil;

/**
 * The {@link TestResult} class.
 */
public class TestResult {

  private final String name;
  private final String className;


  double    time;
  Throwable error;


  private final List<Failure> failures;

  /**
   * Constructs an instance of {@link TestResult}.
   *
   * @param name
   * @param className
   */
  public TestResult(String name, String className) {
    this.name = name;
    this.className = className;
    this.failures = new ArrayList<>();
  }

  /**
   * Gets the test case name .
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Gets the class name.
   */
  public final String getClassName() {
    return this.className;
  }

  /**
   * Gets the {@link Failure}'s.
   */
  public final List<Failure> getFailures() {
    return this.failures;
  }

  /**
   * Gets the execution time in seconds.
   */
  public final double getTime() {
    return this.time;
  }

  /**
   * Gets the execution error.
   */
  public final Throwable getError() {
    return this.error;
  }

  /**
   * Invoked if a failure.
   *
   * @param consumer
   */
  public final void onFailure(Consumer<Failure> consumer) throws Throwable {
    if (!getFailures().isEmpty()) {
      consumer.accept(getFailures().get(0));
    }
  }

  /**
   * Gets the {@link Failure}'s.
   */
  public final void addFailure(Object expected, Object actual, String message, Object... arguments) {
    this.failures.add(new Failure(expected, actual, message, arguments));
  }

  public final boolean assertInt(int expected, int actual, String message, Object... arguments) {
    if (expected == actual) {
      return true;
    }
    addFailure(expected, actual, message, arguments);
    return false;
  }

  public final boolean assertNumber(double expected, double actual, String message, Object... arguments) {
    if (expected == actual) {
      return true;
    }
    addFailure(expected, actual, message, arguments);
    return false;
  }

  public final boolean assertEnum(Enum<?> expected, Enum<?> actual, String message, Object... arguments) {
    if (expected == actual) {
      return true;
    }
    addFailure(expected, actual, message, arguments);
    return false;
  }

  public final boolean assertString(String expected, String actual, String message, Object... arguments) {
    if (expected.startsWith("/") || expected.endsWith("/")) {
      String text = "^" + expected.substring(1, expected.length() - 1) + "$";
      Pattern pattern = Pattern.compile(text);
      if (!pattern.matcher(actual).find()) {
        addFailure(expected, actual, message, arguments);
        return true;
      }
    } else {
      if (!actual.equals(expected)) {
        addFailure(expected, actual, message, arguments);
        return true;
      }
    }
    return true;
  }

  public final void assertJson(String expected, String actual) {
    JsonUtil.assertJson(this, expected, actual);
  }
}
