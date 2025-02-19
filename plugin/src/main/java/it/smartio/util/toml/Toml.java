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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;

/**
 * Utility class for reading and writing TOML v0.4.0. This class internally uses {@link TomlReader}
 * and {@link TomlWriter}.
 *
 * <h1>DateTimes support</h1> <p> The datetime support is more extended than in the TOML
 * specification. The reader and the writer support three kind of datetimes: <ol> <li>Full RFC 3339.
 * Example: 2015-03-20T19:26:00+01:00 => represented as {@link ZonedDateTime}</li> <li>Without local
 * offset. Examples: 2015-03-20T19:26:00 => represented as {@link LocalDateTime}</li> <li>Without
 * time (just the date). Example: 2015-03-20 => represented as {@link LocalDate}</li> </ol> </p>
 * <h1>Lenient bare keys</h1> <p> This library allows "lenient" bare keys by default, as opposite to
 * the "strict" bare keys which are required by the TOML specification. Strict bare keys may only
 * contain letters, numbers, underscores, and dashes (A-Za-z0-9_-). Lenient bare keys may contain
 * any character except those before the space character in the unicode table (tabs, newlines and
 * many unprintables characters), spaces, points, square brackets, number signs and equal signs (. [
 * ] # =). </p> <p> The default setting when reading TOML data is lenient. You may set the behaviour
 * regarding bare keys with the methods {@link #read(String, boolean)} and
 * {@link #read(Reader, int, boolean)}, or by creating a {@link TomlReader} yourself. </p> <p> The
 * {@link TomlWriter} always outputs data t strictly follows the TOML specification. Any key that
 * contains one or more non-strictly valid character is surrounded by quotes. </p>
 *
 * @author TheElectronWill
 *
 */
public interface Toml {

  /**
   * A DateTimeFormatter that uses the TOML format.
   */
  DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
      .optionalStart().appendLiteral('T').append(DateTimeFormatter.ISO_LOCAL_TIME).optionalStart().appendOffsetId()
      .optionalEnd().optionalEnd().toFormatter();

  /**
   * Writes the specified data to a String, in the TOML format.
   *
   * @param data
   */
  static String writeToString(Map<String, Object> data) throws IOException {
    FastStringWriter writer = new FastStringWriter();
    Toml.write(data, writer);
    return writer.toString();
  }

  /**
   * Writes data to a File, in the TOML format and with the UTF-8 encoding. The default indentation
   * parameters are used, ie each indent is one tab character.
   *
   * @param data
   * @param file
   */
  static void write(Map<String, Object> data, File file) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    Toml.write(data, out);
  }

  /**
   * Writes data to an OutputStream, in the TOML format and with the UTF-8 encoding. The default
   * indentation parameters are used, ie each indent is one tab character.
   *
   * @param data
   * @param file
   */
  static void write(Map<String, Object> data, OutputStream out) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    Toml.write(data, writer);
  }

  /**
   * Writes data to a Writer, in the TOML format and with the default parameters, ie each indent is
   * 1 tab character. This is the same as {@code write(data, writer, 1, false)}.
   *
   * @param data
   * @param writer
   */
  static void write(Map<String, Object> data, Writer writer) throws IOException {
    TomlWriter tw = new TomlWriter(writer);
    tw.write(data);
    tw.close();
  }

  /**
   * Writes the specified data to a Writer, in the TOML format and with the specified parameters.
   *
   * @param data
   * @param writer
   * @param indentSize
   */
  static void write(Map<String, Object> data, Writer writer, int indentSize, boolean indentWithSpaces)
      throws IOException {
    TomlWriter tw = new TomlWriter(writer, indentSize, indentWithSpaces);
    tw.write(data);
    tw.close();
  }

  /**
   * Reads a String that contains TOML data. Lenient bare keys are allowed (see {@link Toml}).
   *
   * @param toml
   */
  static Map<String, Object> read(String toml) throws TomlException {
    return Toml.read(toml, false);
  }

  /**
   * Reads a String that contains TOML data.
   *
   * @param toml
   * @param strictAsciiBareKeys
   */
  static Map<String, Object> read(String toml, boolean strictAsciiBareKeys) {
    TomlReader tr = new TomlReader(toml, strictAsciiBareKeys);
    return tr.read();
  }

  /**
   * Reads TOML data from an UTF-8 encoded File. Lenient bare keys are allowed (see {@link Toml}).
   *
   * @param file
   */
  static Map<String, Object> read(File file) throws IOException, TomlException {
    return Toml.read(file, false);
  }

  /**
   * Reads TOML data from an UTF-8 encoded File.
   *
   * @param file
   * @param strictAsciiBareKeys
   */
  static Map<String, Object> read(File file, boolean strictAsciiBareKeys) throws IOException, TomlException {
    return Toml.read(new FileInputStream(file), strictAsciiBareKeys);
  }

  /**
   * Reads TOML data from an UTF-8 encoded InputStream. Lenient bare keys are allowed (see
   * {@link Toml}).
   *
   * @param in
   */
  static Map<String, Object> read(InputStream in) throws IOException, TomlException {
    return Toml.read(in, false);
  }

  /**
   * Reads TOML data from an UTF-8 encoded InputStream.
   *
   * @param in
   * @param strictAsciiBareKeys
   */
  static Map<String, Object> read(InputStream in, boolean strictAsciiBareKeys) throws IOException, TomlException {
    return Toml.read(new InputStreamReader(in, StandardCharsets.UTF_8), in.available(), strictAsciiBareKeys);
  }

  /**
   * Reads TOML data from a Reader. The data is read until the end of the stream is reached.
   *
   * @param in
   * @param bufferSize
   * @param strictAsciiBareKeys
   */
  static Map<String, Object> read(Reader reader, int bufferSize, boolean strictAsciiBareKeys)
      throws IOException, TomlException {
    StringBuilder sb = new StringBuilder(bufferSize);
    char[] buf = new char[8192];
    int read;
    while ((read = reader.read(buf)) != -1) {
      sb.append(buf, 0, read);
    }
    TomlReader tr = new TomlReader(sb.toString(), strictAsciiBareKeys);
    return tr.read();
  }

}
