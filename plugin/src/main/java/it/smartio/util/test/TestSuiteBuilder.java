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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.stream.Stream;

/**
 * The {@link TestSuiteBuilder} class.
 */
public class TestSuiteBuilder implements TestSuite {

  final String          name;
  final Stack<TestCase> testCases  = new Stack<>();
  final Properties      properties = new Properties();


  private String className;


  /**
   * Constructs an instance of {@link TestSuiteBuilder}.
   *
   * @param name
   */
  public TestSuiteBuilder(String name) {
    this.name = name;
  }

  /**
   * Gets the name of the {@link TestSuite}
   */
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Properties getProperties() {
    return this.properties;
  }

  /**
   * Returns a sequential {@code Stream} of {@link TestCase}'s.
   */
  @Override
  public final Stream<TestCase> stream() {
    return ((Stack<TestCase>) (Stack<?>) this.testCases).stream();
  }

  /**
   * Adds a property to the suite.
   *
   * @param name
   * @param value
   */
  public final TestSuiteBuilder addProperty(String name, String value) {
    this.properties.setProperty(name, value);
    return this;
  }

  /**
   * Pushes a new TestCase to the suite
   *
   * @param name
   */
  public final TestSuiteBuilder addTestCase(String name, Test test) {
    this.testCases.push(new TestCase(name, test));
    this.testCases.peek().className = this.className;
    return this;
  }

  /**
   * Set the class name for the current test case
   *
   * @param className
   */
  public final TestSuiteBuilder setClassname(String className) {
    this.className = className;
    return this;
  }


  @Override
  public final TestReport execute() {
    List<TestResult> results = new ArrayList<>();
    for (TestCase testCase : this.testCases) {
      try {
        results.add(testCase.execute());
      } catch (Throwable t) {
        TestResult result = new TestResult(testCase.getName(), testCase.className);
        result.error = t;
        results.add(result);
      }
    }
    return new TestReport(this.name, this.properties, results);
  }

  /**
   * Create a {@link TestSuiteBuilder}.
   */
  public final TestSuite build() {
    return this;
  }
}
