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

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import it.smartio.build.Build;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.git.Repository;
import it.smartio.util.mail.Mailer;


/**
 * The {@link ReportTask} implements an abstract {@link Task} for GIT repositories.
 */
public class ReportTask implements Task {

  private final Pattern      pattern;

  private final String       emailHost;
  private final List<String> emailAddresses;

  /**
   * Constructs an instance of {@link ReportTask}.
   *
   * @param pattern
   * @param emailHost
   * @param emailAddresses
   */
  public ReportTask(String pattern, String emailHost, List<String> emailAddresses) {
    this.pattern = Pattern.compile(pattern.replaceAll("\\.", "\\.").replaceAll("\\*", ".*"));
    this.emailHost = emailHost;
    this.emailAddresses = emailAddresses;
  }

  /**
   * A GIT Task that provides a {@link Repository}.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) {
    File buildDir = new File(context.getEnvironment().get(Build.BUILD_DIR));

    try {
      File zip = new File(buildDir, "report.zip");
      File report = new File(buildDir, "report.html");
      File overview = new File(buildDir, "overview.html");

      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

      String subject = "";
      try (ReportWriterHtml writer = new ReportWriterHtml(report, overview)) {
        UnitHandler handler = new UnitHandler(writer);
        for (File result : buildDir.listFiles(p -> this.pattern.matcher(p.getName()).matches())) {
          parser.parse(result, handler);
        }
        subject =
            String.format("Test Report: Failures(%s), Errors(%s)", handler.getTotalFailure(), handler.getTotalError());
      }

      try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
        ZipEntry entry = new ZipEntry("report.html");
        out.putNextEntry(entry);
        byte[] bytes = Files.readAllBytes(report.toPath());
        out.write(bytes, 0, bytes.length);
        out.closeEntry();
      }

      if (!this.emailAddresses.isEmpty()) {
        Mailer mailer = new Mailer(this.emailHost, null, null);
        mailer.addContent(new String(Files.readAllBytes(overview.toPath())), "text/html");
        mailer.addAttachment(zip);
        mailer.send(subject, this.emailAddresses);
      }
    } catch (IOException | MessagingException | SAXException | ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}
