/*
 * Copyright (c) 2001-2024 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.docs.fop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import it.smartio.docs.Book;
import it.smartio.docs.Renderer;
import it.smartio.docs.fop.config.FoContext;

/**
 * The {@link FoGenerator} class.
 */
public class FoGenerator {

  private String    name;
  private FoContext context;
  private File      target;
  private boolean   debug;

  /**
   * Sets the filename
   */
  public final FoGenerator setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets the {@link FoContext}.
   */
  public final FoGenerator setContext(FoContext context) {
    this.context = context;
    return this;
  }

  /**
   * Sets the target directory.
   */
  public final FoGenerator setTarget(File target) {
    this.target = target;
    return this;
  }

  /**
   * Sets the <code>true</code> if the fo file will be generated
   */
  public final FoGenerator setDebug(boolean debug) {
    this.debug = debug;
    return this;
  }

  /**
   * Writes the {@link Book} to FO.
   *
   * @param book
   * @param config
   * @param writer
   * @throws IOException
   */
  protected static void writeFo(Book book, FoContext config, Writer writer) throws IOException {
    try (PrintWriter printer = new PrintWriter(writer)) {
      Renderer renderer = new FoRenderer(printer, config);
      renderer.render(book);
    }
  }

  /**
   * Writes the FO stream to PDF.
   *
   * @param file
   * @param stream
   * @param factory
   */
  public static void writeFo(File file, InputStream source) throws Exception {
    byte[] bytes = new byte[1048576];
    try (OutputStream target = new FileOutputStream(file)) {
      while (source.available() > 0) {
        int length = source.read(bytes);
        target.write(bytes, 0, length);
      }
    }
  }

  /**
   * Build the markdown based book.
   */
  public final InputStream generate(Book book) throws IOException {
    if (this.debug) {
      String name = this.name;
      File fo = new File(this.target, name + ".fo");
      if (fo.exists()) {
        fo.delete();
      }

      FileWriter writer = new FileWriter(fo);
      FoGenerator.writeFo(book, this.context, writer);
      return new FileInputStream(fo);
    }

    StringWriter writer = new StringWriter();
    FoGenerator.writeFo(book, this.context, writer);
    return new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
  }
}
