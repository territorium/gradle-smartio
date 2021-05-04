/*
 * Copyright (c) 2001-2022 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.build;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import it.smartio.commonmark.MarkdownReader;
import it.smartio.docs.util.Replacer;

/**
 * The {@link AbstractTest} class.
 */
abstract class AbstractTest {

  protected static final Properties PROPERTIES  = new Properties();
  protected static final File       WORKING_DIR = new File("/data/smartIO/develop");

  protected static final File       TARGET      = new File(AbstractTest.WORKING_DIR, "target");


  static {
    DocuTest.PROPERTIES.setProperty("VERSION", "25.04");
    DocuTest.TARGET.mkdirs();
  }

  protected abstract File getSource();

  @Test
  void testPDF() {
    DocumentBuilder builder = new DocumentBuilder(MarkdownTest.WORKING_DIR);
    builder.setConfig(":TOL:");
    builder.setTarget(MarkdownTest.TARGET);
    builder.setSource(getSource().getAbsolutePath());
    builder.addProperties(MarkdownTest.PROPERTIES);
    builder.build();
  }

  @Test
  void testMarkdownMerge() throws IOException {
    MarkdownReader reader = new MarkdownReader(getSource());
    File target = new File(MarkdownTest.TARGET, getSource().getName());

    Replacer replacer = new Replacer(MarkdownTest.PROPERTIES);
    try (FileWriter writer = new FileWriter(target)) {
      writer.write(replacer.replaceAll(reader.readAll()));
    }
  }
}
