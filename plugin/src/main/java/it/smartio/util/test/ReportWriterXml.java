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

import java.io.Writer;
import java.util.Properties;

/**
 * The {@link ReportWriterXml} class.
 */
public class ReportWriterXml extends ReportWriter {

  /**
   * Constructs an instance of {@link ReportWriterXml}.
   *
   */
  public ReportWriterXml(Writer writer) {
    super(writer);
  }

  /**
   * Writes a report for the {@link TestReport}.
   *
   * @param report
   */
  @Override
  public final void write(TestReport report) {
    println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    printf("<testsuite name=\"%s\">\n", report.getName());
    writeEnvironment(report.getProperties());

    for (TestResult r : report) {
      printf("  <testcase name=\"%s\" classname=\"%s\" time=\"%s\"", r.getName(), r.getClassName(), r.getTime());
      if ((r.getError() == null) && r.getFailures().isEmpty()) {
        println("/>");
      } else if (r.getError() != null) {
        println(">");
        printf("    <error message=\"%s\"><![CDATA[", r.getError().getMessage());
        int count = Math.min(r.getError().getStackTrace().length, 20);
        for (int i = 0; i < count; i++) {
          printf("%s%s\n", r.getError().getStackTrace()[i].toString(), ((i + 1) == count) ? "]]>" : "");
        }
        println("    </error>");
      } else {
        println(">");
        r.getFailures().forEach(f -> writeFailure(f));
        println("  </testcase>");
      }
    }
    println("</testsuite>");
  }

  /**
   * Write the environment variables.
   *
   * @param properties
   */
  protected void writeEnvironment(Properties properties) {
    if (!properties.isEmpty()) {
      println("  <properties>");
      properties.keySet().forEach(k -> printf("    <property name=\"%s\" value=\"%s\"/>\n", k, properties.get(k)));
      println("  </properties>");
    }
  }

  /**
   * Write a {@link Failure}.
   *
   * @param failure
   */
  protected void writeFailure(Failure failure) {
    printf("    <failure type=\"fail\" message=\"%s\">\n", failure.getMessage());
    printf("<![CDATA[Actual: %s\n", failure.getActual());
    printf("Expected: %s]]>\n", failure.getExpected());
    println("    </failure>");
  }
}
