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

package it.smartio.util.toml;


import java.io.Writer;

/**
 * A Writer writing in a StringBuilder.
 */
public class FastStringWriter extends Writer {

  /**
   * The underlying StringBuilder. Everything is appended to it.
   */
  private final StringBuilder builder;

  /**
   * Creates a new FastStringWriter with a default StringBuilder
   */
  public FastStringWriter() {
    this.builder = new StringBuilder();
  }

  /**
   * Creates a new FastStringWriter with a given StringBuilder. It will append everything to this
   * StringBuilder.
   *
   * @param builder
   */
  public FastStringWriter(StringBuilder builder) {
    this.builder = builder;
  }

  /**
   * Returns the underlying StringBuilder.
   */
  public StringBuilder getBuilder() {
    return this.builder;
  }

  /**
   * Returns the content of the underlying StringBuilder, as a String.
   */
  @Override
  public String toString() {
    return this.builder.toString();
  }

  @Override
  public FastStringWriter append(char c) {
    this.builder.append(c);
    return this;
  }

  @Override
  public FastStringWriter append(CharSequence csq, int start, int end) {
    this.builder.append(csq, start, end);
    return this;
  }

  @Override
  public FastStringWriter append(CharSequence csq) {
    this.builder.append(csq);
    return this;
  }

  @Override
  public void write(String str, int off, int len) {
    this.builder.append(str, off, off + len);
  }

  @Override
  public void write(String str) {
    this.builder.append(str);
  }

  @Override
  public void write(char[] cbuf, int off, int len) {
    this.builder.append(cbuf, off, len);
  }

  @Override
  public void write(int c) {
    this.builder.append(c);
  }

  /**
   * This method does nothing.
   */
  @Override
  public void flush() {}

  /**
   * This method does nothing.
   */
  @Override
  public void close() {}
}