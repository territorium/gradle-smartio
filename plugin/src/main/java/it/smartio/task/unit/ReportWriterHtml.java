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

package it.smartio.task.unit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The {@link ReportWriterHtml} class.
 */
public class ReportWriterHtml implements ReportWriter {

  private final PrintWriter report;
  private final PrintWriter overview;


  private int totalSuccess;
  private int totalSkipped;
  private int totalFailure;
  private int totalError;

  /**
   * Constructs an instance of {@link ReportWriterHtml}.
   *
   * @param report
   * @param overview
   */
  public ReportWriterHtml(File reportFile, File overviewFile) throws FileNotFoundException {
    this.report = new PrintWriter(reportFile);
    this.overview = new PrintWriter(overviewFile);

    this.report.println("<html>");
    this.report.println("<head>");
    this.report.println("<style>");
    this.report.println("  body { font-family: \"sans-serif\"; }");
    this.report.println("  pre { margin: 0; }");
    this.report.println("  .suite { border-top: 1px solid gray;}");
    this.report.println("  .header { border-bottom: 3px solid black; }");
    this.report.println("  .total { border-top: 3px solid black; }");

    this.report.println("  .suite>.name>div { display: inline-block; width: 120px; text-align: center; }");
    this.report.println("  .suite>.name>.title { width: calc(100% - 480px); text-align: left; }");
    this.report.println("  .success { background: #ccffcc; }");
    this.report.println("  .warning { background: #ffffbb; }");
    this.report.println("  .skipped { background: #ccf5ff; }");
    this.report.println("  .failure { background: #ffd7af; }");
    this.report.println("  .error { background: #ffbbbb; }");
    this.report.println("  .tests { display: none; }");
    this.report.println("  .suite.open .tests { display: block; }");
    this.report.println("  .header .open { display: block; font-weight: bold; }");
    this.report.println("</style>");
    this.report.println("<script>");
    this.report.println(
        "function toogle(elem) { if(elem.classList.contains('open')) elem.classList.remove('open'); else elem.classList.add('open'); }");
    this.report.println("function toogle2(elem, name) { if(elem.classList.contains('open')) "
        + "{ elem.classList.remove('open'); document.querySelectorAll('.test.' + name).forEach(function(n, i) { n.style.display = 'none'; });"
        + "} else {"
        + " elem.classList.add('open'); document.querySelectorAll('.test.' + name).forEach(function(n, i) { n.style.display = 'block';}); }"
        + "}");

    this.report.println("</script>");
    this.report.println("</head>");
    this.report.println("<body>");

    this.overview.println("<html>");
    this.overview.println("<head>");
    this.overview.println("<style>");
    this.overview.println("  body { font-family: \"sans-serif\"; }");
    this.overview.println("  .suite { border-top: 1px solid gray; }");
    this.overview.println("  .total { border-top: 3px solid black; }");

    this.overview.println("  .suite>.name>div { display: inline-block; width: 120px; text-align: center; }");
    this.overview.println("  .suite>.name>.title { width: calc(100% - 480px); text-align: left; }");
    this.overview.println("  .success { background: #ccffcc; }");
    this.overview.println("  .warning { background: #ffffbb; }");
    this.overview.println("  .skipped { background: #ccf5ff; }");
    this.overview.println("  .failure { background: #ffd7af; }");
    this.overview.println("  .error { background: #ffbbbb; }");
    this.overview.println("  .test { display: none; }");
    this.overview.println("  .test.failure,.test.error { display: block; }");
    this.overview.println("</style>");
    this.overview.println("</head>");
    this.overview.println("<body>");

    this.report.println("<div class=\"suite header\"><div class=\"name\">");
    this.report.print("<div class=\"title\">Test Suite</div>");
    this.report.print("<div class=\"success open\" onclick=\"toogle2(this, 'success');\">Success</div>");
    this.report.print("<div class=\"skipped open\" onclick=\"toogle2(this, 'skipped');\">Skipped</div>");
    this.report.print("<div class=\"failure open\" onclick=\"toogle2(this, 'failure');\">Failure</div>");
    this.report.print("<div class=\"error open\" onclick=\"toogle2(this, 'error');\">Error</div>");
    this.report.println("</div></div>");

    this.overview.println("<div class=\"suite header\"><div class=\"name\">");
    this.overview.print("<div class=\"title\">Test Suite</div>");
    this.overview.print("<div class=\"success open\">Success</div>");
    this.overview.print("<div class=\"skipped open\">Skipped</div>");
    this.overview.print("<div class=\"failure open\">Failure</div>");
    this.overview.print("<div class=\"error open\">Error</div>");
    this.overview.println("</div></div>");
  }

  @Override
  public final void addTestSuite(String name, int success, int skipped, int failure, int error) {
    this.report.println("<div class=\"suite\" onclick=\"toogle(this);\">");
    this.report.print("<div class=\"name\"><div class=\"title\">" + name + "</div>");

    this.overview.println("<div class=\"suite\">");
    this.overview.print("<div class=\"name\"><div class=\"title\">" + name + "</div>");

    this.report.print("<div class=\"success\">" + success + "</div>");
    this.report.print("<div class=\"skipped\">" + skipped + "</div>");
    this.report.print("<div class=\"failure\">" + failure + "</div>");
    this.report.print("<div class=\"error\">" + error + "</div>");
    this.report.println("</div>");
    this.report.println("<div class=\"tests\">");

    this.overview.print("<div class=\"success\">" + success + "</div>");
    this.overview.print("<div class=\"skipped\">" + skipped + "</div>");
    this.overview.print("<div class=\"failure\">" + failure + "</div>");
    this.overview.print("<div class=\"error\">" + error + "</div>");
    this.overview.println("</div>");


    this.totalSuccess += success;
    this.totalSkipped += skipped;
    this.totalFailure += failure;
    this.totalError += error;
  }


  @Override
  public final void addTestUnit(String name, String state, String message, String output) {
    this.report.println("<div class=\"test " + state + "\">");
    this.report.println("<div class=\"name\">" + name + "</div>");
    if (message != null) {
      this.report.println("<div>" + message + "</div>");
    }
    if (!output.isEmpty()) {
      this.report.println("<pre>" + output + "</pre>");
    }
    this.report.println("</div>");
  }

  @Override
  public final void endTestSuite() {
    this.report.println("</div>");
    this.report.println("</div>");
    this.overview.println("</div>");
  }

  /**
   * Closes this stream and releases any system resources associated with it. If the stream is
   * already closed then invoking this method has no effect.
   */
  @Override
  public final void close() throws IOException {
    this.report.println("<div class=\"suite total\"><div class=\"name\">");
    this.report.print("<div class=\"title\">TOTAL</div>");
    this.report.print("<div class=\"success\">" + this.totalSuccess + "</div>");
    this.report.print("<div class=\"skipped\">" + this.totalSkipped + "</div>");
    this.report.print("<div class=\"failure\">" + this.totalFailure + "</div>");
    this.report.print("<div class=\"error\">" + this.totalError + "</div>");
    this.report.println("</div></div>");
    this.report.println("</body>\n</html>");

    this.overview.println("<div class=\"suite total\"><div class=\"name\">");
    this.overview.print("<div class=\"title\">TOTAL</div>");
    this.overview.print("<div class=\"success\">" + this.totalSuccess + "</div>");
    this.overview.print("<div class=\"skipped\">" + this.totalSkipped + "</div>");
    this.overview.print("<div class=\"failure\">" + this.totalFailure + "</div>");
    this.overview.print("<div class=\"error\">" + this.totalError + "</div>");
    this.overview.println("</div></div>");
    this.overview.println("</body>\n</html>");

    this.report.close();
    this.overview.close();
  }
}
