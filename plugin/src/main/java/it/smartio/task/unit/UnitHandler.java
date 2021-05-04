
package it.smartio.task.unit;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class UnitHandler extends DefaultHandler2 {

  private final ReportWriter writer;


  private String testCaseName;
  private State  testCaseState;


  private String       message;
  private StringBuffer buffer = null;


  private int totalSuccess;
  private int totalSkipped;
  private int totalFailure;
  private int totalError;


  private enum State {
    SUCCESS,
    SKIPPED,
    WARNING,
    FAILURE,
    ERROR,
  }

  /**
   * Constructs an instance of {@link UnitHandler}.
   *
   * @param writer
   */
  public UnitHandler(ReportWriter writer) {
    this.writer = writer;
  }

  /**
   * Gets the total failures.
   */
  public final int getTotalFailure() {
    return this.totalFailure;
  }

  /**
   * Gets the total errors.
   */
  public final int getTotalError() {
    return this.totalError;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    switch (qName) {
      case "testsuite":
        int tests = Integer.valueOf(attributes.getValue("tests"));
        int skipped = attributes.getValue("skipped") == null ? 0 : Integer.valueOf(attributes.getValue("skipped"));
        int errors = attributes.getValue("errors") == null ? 0 : Integer.valueOf(attributes.getValue("errors"));
        int failures = attributes.getValue("failures") == null ? 0 : Integer.valueOf(attributes.getValue("failures"));
        int success = tests - skipped - errors - failures;

        this.writer.addTestSuite(attributes.getValue("name"), success, skipped, failures, errors);

        this.totalSuccess += success;
        this.totalSkipped += skipped;
        this.totalFailure += failures;
        this.totalError += errors;
        break;

      case "testcase":
        this.testCaseName = attributes.getValue("name");
        this.testCaseState = State.SUCCESS;
        this.buffer = new StringBuffer();
        this.message = null;
        break;

      default:
        break;
    }


    if (qName.equalsIgnoreCase("error")) {
      this.testCaseState = State.ERROR;
      this.message = attributes.getValue("message");
    }

    if (qName.equalsIgnoreCase("failure")) {
      this.message = null;
      this.testCaseState = State.FAILURE;
      this.message = attributes.getValue("message");
    }

    if (qName.equalsIgnoreCase("skipped")) {
      this.message = null;
      this.testCaseState = State.SKIPPED;
      this.message = attributes.getValue("message");
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (this.buffer != null) {
      this.buffer.append(new String(ch, start, length));
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    switch (qName) {
      case "testcase":
        this.writer.addTestUnit(this.testCaseName, this.testCaseState.name().toLowerCase(), this.message,
            this.buffer.toString());
        break;

      case "testsuite":
        this.writer.endTestSuite();
        break;
    }
  }
}
