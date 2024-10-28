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


import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Class for writing TOML v0.4.0. <h1>DateTimes support</h1> <p> Any {@link TemporalAccessor} may be
 * added in a Map passed to this writer, this writer can only write three kind of datetimes:
 * {@link LocalDate}, {@link LocalDateTime} and {@link ZonedDateTime}. </p> <h1>Lenient bare
 * keys</h1> <p> The {@link TomlWriter} always outputs data that strictly follows the TOML
 * specification. Any key that contains one or more non-strictly valid character is surrounded by
 * quotes. </p>
 *
 */
class TomlWriter {

  private final Writer             writer;
  private final int                indentSize;
  private final char               indentCharacter;
  private final String             lineSeparator;
  private final LinkedList<String> tablesNames = new LinkedList<>();
  private int                      lineBreaks  = 0, indentationLevel = -1;// -1 to prevent indenting
                                                                          // the first level

  /**
   * Creates a new TomlWriter with the defaults parameters. The system line separator is used (ie
   * '\n' on Linux and OSX, "\r\n" on Windows). This is exactly the same as
   * {@code TomlWriter(writer, 1, false, System.lineSeparator()}.
   *
   * @param writer
   */
  public TomlWriter(Writer writer) {
    this(writer, 1, false, System.lineSeparator());
  }

  /**
   * Creates a new TomlWriter with the specified parameters. The system line separator is used (ie
   * '\n' on Linux and OSX, "\r\n" on Windows). This is exactly the same as
   * {@code TomlWriter(writer, indentSize, indentWithSpaces, System.lineSeparator())}.
   *
   * @param writer
   * @param indentSize
   * @param indentWithSpaces
   */
  public TomlWriter(Writer writer, int indentSize, boolean indentWithSpaces) {
    this(writer, indentSize, indentWithSpaces, System.lineSeparator());
  }

  /**
   * Creates a new TomlWriter with the specified parameters.
   *
   * @param writer
   * @param indentSize
   * @param indentWithSpaces
   * @param lineSeparator
   */
  public TomlWriter(Writer writer, int indentSize, boolean indentWithSpaces, String lineSeparator) {
    this.writer = writer;
    this.indentSize = indentSize;
    this.indentCharacter = indentWithSpaces ? ' ' : '\t';
    this.lineSeparator = lineSeparator;
  }

  /**
   * Closes the underlying writer, flushing it first.
   */
  public void close() throws IOException {
    this.writer.close();
  }

  /**
   * Flushes the underlying writer.
   */
  public void flush() throws IOException {
    this.writer.flush();
  }

  /**
   * Writes the specified data in the TOML format.
   *
   * @param data
   */
  public void write(Map<String, Object> data) throws IOException {
    writeTableContent(data);
  }

  private void writeTableName() throws IOException {
    Iterator<String> it = this.tablesNames.iterator();
    while (it.hasNext()) {
      String namePart = it.next();
      writeKey(namePart);
      if (it.hasNext()) {
        write('.');
      }
    }
  }

  private void writeTableContent(Map<String, Object> table) throws IOException {
    writeTableContent(table, true);
    writeTableContent(table, false);
  }

  /**
   * Writes the content of a table.
   *
   * @param table
   * @param simpleValues
   */
  private void writeTableContent(Map<String, Object> table, boolean simpleValues) throws IOException {
    for (Map.Entry<String, Object> entry : table.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Collection) {// array
        Collection c = (Collection) value;
        if (!c.isEmpty() && (c.iterator().next() instanceof Map)) {// array of tables
          if (simpleValues) {
            continue;
          }
          this.tablesNames.addLast(name);
          this.indentationLevel++;
          for (Object element : c) {
            indent();
            write("[[");
            writeTableName();
            write("]]\n");
            Map<String, Object> map = (Map) element;
            writeTableContent(map);
          }
          this.indentationLevel--;
          this.tablesNames.removeLast();
        } else {// normal array
          if (!simpleValues) {
            continue;
          }
          indent();
          writeKey(name);
          write(" = ");
          writeArray(c);
        }
      } else if (value instanceof Object[]) {// array
        Object[] array = (Object[]) value;
        if ((array.length > 0) && (array[0] instanceof Map)) {// array of tables
          if (simpleValues) {
            continue;
          }
          this.tablesNames.addLast(name);
          this.indentationLevel++;
          for (Object element : array) {
            indent();
            write("[[");
            writeTableName();
            write("]]\n");
            Map<String, Object> map = (Map) element;
            writeTableContent(map);
          }
          this.indentationLevel--;
          this.tablesNames.removeLast();
        } else {// normal array
          if (!simpleValues) {
            continue;
          }
          indent();
          writeKey(name);
          write(" = ");
          writeArray(array);
        }
      } else if (value instanceof Map) {// table
        if (simpleValues) {
          continue;
        }
        this.tablesNames.addLast(name);
        this.indentationLevel++;

        indent();
        write('[');
        writeTableName();
        write(']');
        newLine();
        writeTableContent((Map) value);

        this.indentationLevel--;
        this.tablesNames.removeLast();
      } else {// simple value
        if (!simpleValues) {
          continue;
        }
        indent();
        writeKey(name);
        write(" = ");
        writeValue(value);
      }
      newLine();
    }
    newLine();
  }

  private void writeKey(String key) throws IOException {
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (!(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || ((c >= '0') && (c <= '9')) || (c == '-')
          || (c == '_'))) {
        writeString(key);
        return;
      }
    }
    write(key);
  }

  private void writeString(String str) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append('"');
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      TomlWriter.addEscaped(c, sb);
    }
    sb.append('"');
    write(sb.toString());
  }

  private void writeArray(Collection c) throws IOException {
    write('[');
    for (Object element : c) {
      writeValue(element);
      write(", ");
    }
    write(']');
  }

  private void writeArray(Object[] array) throws IOException {
    write('[');
    for (Object element : array) {
      writeValue(element);
      write(", ");
    }
    write(']');
  }

  private void writeArray(byte[] array) throws IOException {
    write('[');
    for (byte element : array) {
      write(String.valueOf(element));
      write(", ");
    }
    write(']');
  }

  private void writeArray(short[] array) throws IOException {
    write('[');
    for (short element : array) {
      write(String.valueOf(element));
      write(", ");
    }
    write(']');
  }

  private void writeArray(char[] array) throws IOException {
    write('[');
    for (char element : array) {
      write(String.valueOf(element));
      write(", ");
    }
    write(']');
  }

  private void writeArray(int[] array) throws IOException {
    write('[');
    for (int element : array) {
      write(String.valueOf(element));
      write(", ");
    }
    write(']');
  }

  private void writeArray(long[] array) throws IOException {
    write('[');
    for (long element : array) {
      write(String.valueOf(element));
      write(", ");
    }
    write(']');
  }

  private void writeArray(float[] array) throws IOException {
    write('[');
    for (float element : array) {
      write(String.valueOf(element));
      write(", ");
    }
    write(']');
  }

  private void writeArray(double[] array) throws IOException {
    write('[');
    for (double element : array) {
      write(String.valueOf(element));
      write(", ");
    }
    write(']');
  }

  private void writeValue(Object value) throws IOException {
    if (value instanceof String) {
      writeString((String) value);
    } else if ((value instanceof Number) || (value instanceof Boolean)) {
      write(value.toString());
    } else if (value instanceof TemporalAccessor) {
      String formatted = Toml.DATE_FORMATTER.format((TemporalAccessor) value);
      if (formatted.endsWith("T"))// If the last character is a 'T'
      {
        formatted = formatted.substring(0, formatted.length() - 1);// removes it because it's
                                                                   // invalid.
      }
      write(formatted);
    } else if (value instanceof Collection) {
      writeArray((Collection) value);
    } else if (value instanceof int[]) {
      writeArray((int[]) value);
    } else if (value instanceof byte[]) {
      writeArray((byte[]) value);
    } else if (value instanceof short[]) {
      writeArray((short[]) value);
    } else if (value instanceof char[]) {
      writeArray((char[]) value);
    } else if (value instanceof long[]) {
      writeArray((long[]) value);
    } else if (value instanceof float[]) {
      writeArray((float[]) value);
    } else if (value instanceof double[]) {
      writeArray((double[]) value);
    } else if (value instanceof Map) {// should not happen because an array of tables is detected by
      // writeTableContent()
      throw new IOException("Unexpected value " + value);
    } else {
      throw new TomlException("Unsupported value of type " + value.getClass().getCanonicalName());
    }
  }

  private void newLine() throws IOException {
    if (this.lineBreaks <= 1) {
      this.writer.write(this.lineSeparator);
      this.lineBreaks++;
    }
  }

  private void write(char c) throws IOException {
    this.writer.write(c);
    this.lineBreaks = 0;
  }

  private void write(String str) throws IOException {
    this.writer.write(str);
    this.lineBreaks = 0;
  }

  private void indent() throws IOException {
    for (int i = 0; i < this.indentationLevel; i++) {
      for (int j = 0; j < this.indentSize; j++) {
        write(this.indentCharacter);
      }
    }
  }

  static void addEscaped(char c, StringBuilder sb) {
    switch (c) {
      case '\b':
        sb.append("\\b");
        break;
      case '\t':
        sb.append("\\t");
        break;
      case '\n':
        sb.append("\\n");
        break;
      case '\\':
        sb.append("\\\\");
        break;
      case '\r':
        sb.append("\\r");
        break;
      case '\f':
        sb.append("\\f");
        break;
      case '"':
        sb.append("\\\"");
        break;
      default:
        sb.append(c);
        break;
    }
  }
}