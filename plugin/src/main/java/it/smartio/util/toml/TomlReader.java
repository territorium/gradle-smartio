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


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for reading TOML v0.4.0.
 *
 * <pre>
 * <h1>DateTimes support</h1> <p> The datetime support is more extended than in the TOML
 * specification. This reader supports three kind of datetimes: <ol> <li>Full RFC 3339. Examples:
 * 1979-05-27T07:32:00Z, 1979-05-27T00:32:00-07:00, 1979-05-27T00:32:00.999999-07:00</li>
 * <li>Without local offset. Examples: 1979-05-27T07:32:00, 1979-05-27T00:32:00.999999</li>
 * <li>Without time (just the date). Example: 2015-03-20</li> </ol> Moreover, parsing datetimes
 * gives different objects according to the informations provided. For example, 2015-03-20 is parsed
 * as a {@link LocalDate}, 2015-03-20T19:04:35 as a {@link LocalDateTime}, and
 * 2015-03-20T19:04:35+01:00 as a {@link ZonedDateTime}. </p> <h1>Lenient bare keys</h1> <p> This
 * library allows "lenient" bare keys by default, as opposite to the "strict" bare keys required by
 * the TOML specification. Strict bare keys may only contain letters, numbers, underscores, and
 * dashes (A-Za-z0-9_-). Lenient bare keys may contain any character except those below the space
 * character ' ' in the unicode table, '.', '[', ']' and '='. The behaviour of TomlReader regarding
 * bare keys is set in its constructor. </p>
 *
 */
class TomlReader {

  private final String  data;
  private final boolean strictAsciiBareKeys;
  private int           pos  = 0;           // current position
  private int           line = 1;           // current line

  /**
   * Creates a new TomlReader.
   *
   * @param data
   * @param strictAsciiBareKeys
   */
  public TomlReader(String data, boolean strictAsciiBareKeys) {
    this.data = data;
    this.strictAsciiBareKeys = strictAsciiBareKeys;
  }

  private boolean hasNext() {
    return this.pos < this.data.length();
  }

  private char next() {
    return this.data.charAt(this.pos++);
  }

  private char nextUseful(boolean skipComments) {
    char c = ' ';
    while (hasNext()
        && ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n') || (((c == '#') || (c == ';')) && skipComments))) {
      c = next();
      if (skipComments && ((c == '#') || (c == ';'))) {
        int nextLinebreak = this.data.indexOf('\n', this.pos);
        if (nextLinebreak == -1) {
          this.pos = this.data.length();
        } else {
          this.pos = nextLinebreak + 1;
          this.line++;
        }
      } else if (c == '\n') {
        this.line++;
      }
    }
    return c;
  }

  private char nextUsefulOrLinebreak() {
    char c = ' ';
    while ((c == ' ') || (c == '\t') || (c == '\r')) {
      if (!hasNext()) { // fixes error when no '\n' at the end of the file
        return '\n';
      }
      c = next();
    }
    if (c == '\n') {
      this.line++;
    }
    return c;
  }

  private Object nextValue(char firstChar) {
    switch (firstChar) {
      case '+':
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        return nextNumberOrDate(firstChar);
      case '"':
        if ((this.pos + 1) < this.data.length()) {
          char c2 = this.data.charAt(this.pos);
          char c3 = this.data.charAt(this.pos + 1);
          if ((c2 == '"') && (c3 == '"')) {
            this.pos += 2;
            return nextBasicMultilineString();
          }
        }
        return nextBasicString();
      case '\'':
        if ((this.pos + 1) < this.data.length()) {
          char c2 = this.data.charAt(this.pos);
          char c3 = this.data.charAt(this.pos + 1);
          if ((c2 == '\'') && (c3 == '\'')) {
            this.pos += 2;
            return nextLiteralMultilineString();
          }
        }
        return nextLiteralString();
      case '[':
        return nextArray();
      case '{':
        return nextInlineTable();
      case 't':// Must be "true"
        if (((this.pos + 3) > this.data.length()) || (next() != 'r') || (next() != 'u') || (next() != 'e')) {
          throw new TomlException("Invalid value at line " + this.line);
        }
        return true;
      case 'f':// Must be "false"
        if (((this.pos + 4) > this.data.length()) || (next() != 'a') || (next() != 'l') || (next() != 's')
            || (next() != 'e')) {
          throw new TomlException("Invalid value at line " + this.line);
        }
        return false;
      default:
        throw new TomlException("Invalid character '" + toString(firstChar) + "' at line " + this.line);
    }
  }

  public Map<String, Object> read() {
    Map<String, Object> map = nextTableContent();

    if (!hasNext() && (this.pos > 0) && (this.data.charAt(this.pos - 1) == '[')) {
      throw new TomlException("Invalid table declaration at line " + this.line + ": it never ends");
    }

    while (hasNext()) {
      char c = nextUseful(true);
      boolean twoBrackets;
      if (c == '[') {
        twoBrackets = true;
        c = nextUseful(false);
      } else {
        twoBrackets = false;
      }
      this.pos--;

      // --- Reads the key --
      List<String> keyParts = new ArrayList<>(4);
      boolean insideSquareBrackets = true;
      while (insideSquareBrackets) {
        if (!hasNext()) {
          throw new TomlException("Invalid table declaration at line " + this.line + ": it never ends");
        }

        String name = null;
        char nameFirstChar = nextUseful(false);
        switch (nameFirstChar) {
          case '"': {
            if ((this.pos + 1) < this.data.length()) {
              char c2 = this.data.charAt(this.pos);
              char c3 = this.data.charAt(this.pos + 1);
              if ((c2 == '"') && (c3 == '"')) {
                this.pos += 2;
                name = nextBasicMultilineString();
              }
            }
            if (name == null) {
              name = nextBasicString();
            }
            break;
          }
          case '\'': {
            if ((this.pos + 1) < this.data.length()) {
              char c2 = this.data.charAt(this.pos);
              char c3 = this.data.charAt(this.pos + 1);
              if ((c2 == '\'') && (c3 == '\'')) {
                this.pos += 2;
                name = nextLiteralMultilineString();
              }
            }
            if (name == null) {
              name = nextLiteralString();
            }
            break;
          }
          default:
            this.pos--;// to include the first (already read) non-space character
            name = nextBareKey(']', '.').trim();
            if (this.data.charAt(this.pos) == ']') {
              if (!name.isEmpty()) {
                keyParts.add(name);
              }
              insideSquareBrackets = false;
            } else if (name.isEmpty()) {
              throw new TomlException("Invalid empty key at line " + this.line);
            }

            this.pos++;// to go after the character we stopped at in nextBareKey()
            break;
        }
        if (insideSquareBrackets) {
          keyParts.add(name.trim());
        }
      }

      // -- Checks --
      if (keyParts.isEmpty()) {
        throw new TomlException("Invalid empty key at line " + this.line);
      }

      if (twoBrackets && (next() != ']')) {// 2 brackets at the start but only one at the end!
        throw new TomlException("Missing character ']' at line " + this.line);
      }

      // -- Reads the value (table content) --
      Map<String, Object> value = nextTableContent();

      // -- Saves the value --
      Map<String, Object> valueMap = map;// the map that contains the value
      for (int i = 0; i < (keyParts.size() - 1); i++) {
        String part = keyParts.get(i);
        Object child = valueMap.get(part);
        Map<String, Object> childMap;
        if (child == null) {// implicit table
          childMap = new HashMap<>(4);
          valueMap.put(part, childMap);
        } else if (child instanceof Map) {// table
          childMap = (Map) child;
        } else {// array
          List<Map> list = (List) child;
          childMap = list.get(list.size() - 1);
        }
        valueMap = childMap;
      }
      if (twoBrackets) {// element of a table array
        String name = keyParts.get(keyParts.size() - 1);
        Collection<Map> tableArray = (Collection) valueMap.get(name);
        if (tableArray == null) {
          tableArray = new ArrayList<>(2);
          valueMap.put(name, tableArray);
        }
        tableArray.add(value);
      } else {// just a table
        valueMap.put(keyParts.get(keyParts.size() - 1), value);
      }

    }
    return map;
  }

  private List nextArray() {
    ArrayList<Object> list = new ArrayList<>();
    while (true) {
      char c = nextUseful(true);
      if (c == ']') {
        this.pos++;
        break;
      }
      Object value = nextValue(c);
      if (!list.isEmpty() && !(list.get(0).getClass().isAssignableFrom(value.getClass()))) {
        throw new TomlException("Invalid array at line " + this.line + ": all the values must have the same type");
      }
      list.add(value);

      char afterEntry = nextUseful(true);
      if (afterEntry == ']') {
        this.pos++;
        break;
      }
      if (afterEntry != ',') {
        throw new TomlException("Invalid array at line " + this.line + ": expected a comma after each value");
      }
    }
    this.pos--;
    list.trimToSize();
    return list;
  }

  private Map<String, Object> nextInlineTable() {
    Map<String, Object> map = new HashMap<>();
    while (true) {
      char nameFirstChar = nextUsefulOrLinebreak();
      String name = null;
      switch (nameFirstChar) {
        case '}':
          return map;
        case '"': {
          if ((this.pos + 1) < this.data.length()) {
            char c2 = this.data.charAt(this.pos);
            char c3 = this.data.charAt(this.pos + 1);
            if ((c2 == '"') && (c3 == '"')) {
              this.pos += 2;
              name = nextBasicMultilineString();
            }
          }
          if (name == null) {
            name = nextBasicString();
          }
          break;
        }
        case '\'': {
          if ((this.pos + 1) < this.data.length()) {
            char c2 = this.data.charAt(this.pos);
            char c3 = this.data.charAt(this.pos + 1);
            if ((c2 == '\'') && (c3 == '\'')) {
              this.pos += 2;
              name = nextLiteralMultilineString();
            }
          }
          if (name == null) {
            name = nextLiteralString();
          }
          break;
        }
        default:
          this.pos--;// to include the first (already read) non-space character
          name = nextBareKey(' ', '\t', '=');
          if (name.isEmpty()) {
            throw new TomlException("Invalid empty key at line " + this.line);
          }
          break;
      }

      char separator = nextUsefulOrLinebreak();// tries to find the '=' sign
      if (separator != '=') {
        throw new TomlException(
            "Invalid character '" + toString(separator) + "' at line " + this.line + ": expected '='");
      }

      char valueFirstChar = nextUsefulOrLinebreak();
      Object value = nextValue(valueFirstChar);
      map.put(name, value);

      char after = nextUsefulOrLinebreak();
      if ((after == '}') || !hasNext()) {
        return map;
      } else if (after != ',') {
        throw new TomlException("Invalid inline table at line " + this.line + ": missing comma");
      }
    }
  }

  private Map<String, Object> nextTableContent() {
    Map<String, Object> map = new HashMap<>();
    while (true) {
      char nameFirstChar = nextUseful(true);
      if (!hasNext() || (nameFirstChar == '[')) {
        return map;
      }
      String name = null;
      switch (nameFirstChar) {
        case '"': {
          if ((this.pos + 1) < this.data.length()) {
            char c2 = this.data.charAt(this.pos);
            char c3 = this.data.charAt(this.pos + 1);
            if ((c2 == '"') && (c3 == '"')) {
              this.pos += 2;
              name = nextBasicMultilineString();
            }
          }
          if (name == null) {
            name = nextBasicString();
          }
          break;
        }
        case '\'': {
          if ((this.pos + 1) < this.data.length()) {
            char c2 = this.data.charAt(this.pos);
            char c3 = this.data.charAt(this.pos + 1);
            if ((c2 == '\'') && (c3 == '\'')) {
              this.pos += 2;
              name = nextLiteralMultilineString();
            }
          }
          if (name == null) {
            name = nextLiteralString();
          }
          break;
        }
        default:
          this.pos--;// to include the first (already read) non-space character
          name = nextBareKey(' ', '\t', '=');
          if (name.isEmpty()) {
            throw new TomlException("Invalid empty key at line " + this.line);
          }
          break;
      }
      char separator = nextUsefulOrLinebreak();// tries to find the '=' sign
      if (separator != '=') { // an other character
        throw new TomlException(
            "Invalid character '" + toString(separator) + "' at line " + this.line + ": expected '='");
      }

      char valueFirstChar = nextUsefulOrLinebreak();
      if (valueFirstChar == '\n') {
        throw new TomlException("Invalid newline before the value at line " + this.line);
      }
      Object value = nextValue(valueFirstChar);

      char afterEntry = nextUsefulOrLinebreak();
      if ((afterEntry == '#') || (afterEntry == ';')) {
        this.pos--;// to make the next nextUseful() call read the # character
      } else if (afterEntry != '\n') {
        throw new TomlException(
            "Invalid character '" + toString(afterEntry) + "' after the value at line " + this.line);
      }
      if (map.containsKey(name)) {
        throw new TomlException("Duplicate key \"" + name + "\"");
      }

      map.put(name, value);
    }
  }

  private Object nextNumberOrDate(char first) {
    boolean maybeDouble = true, maybeInteger = true, maybeDate = true;
    StringBuilder sb = new StringBuilder();
    sb.append(first);
    char c;
    whileLoop:
    while (hasNext()) {
      c = next();
      switch (c) {
        case ':':
        case 'T':
        case 'Z':
          maybeInteger = maybeDouble = false;
          break;
        case 'e':
        case 'E':
          maybeInteger = maybeDate = false;
          break;
        case '.':
          maybeInteger = false;
          break;
        case '-':
          if ((c == '-') && (this.pos != 0) && (this.data.charAt(this.pos - 1) != 'e')
              && (this.data.charAt(this.pos - 1) != 'E')) {
            maybeInteger = maybeDouble = false;
          }
          break;
        case ',':
        case ' ':
        case '\t':
        case '\n':
        case '\r':
        case ']':
        case '}':
          this.pos--;
          break whileLoop;
      }
      if (c == '_') {
        maybeDate = false;
      } else {
        sb.append(c);
      }
    }
    String valueStr = sb.toString();
    try {
      if (maybeInteger) {
        if (valueStr.length() < 10) {
          return Integer.parseInt(valueStr);
        }
        return Long.parseLong(valueStr);
      }

      if (maybeDouble) {
        return Double.parseDouble(valueStr);
      }

      if (maybeDate) {
        return Toml.DATE_FORMATTER.parseBest(valueStr, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
      }

    } catch (Exception ex) {
      throw new TomlException("Invalid value: \"" + valueStr + "\" at line " + this.line, ex);
    }

    throw new TomlException("Invalid value: \"" + valueStr + "\" at line " + this.line);
  }

  private String nextBareKey(char... allowedEnds) {
    String keyName;
    for (int i = this.pos; i < this.data.length(); i++) {
      char c = this.data.charAt(i);
      for (char allowedEnd : allowedEnds) {
        if (c == allowedEnd) {// checks if this character allowed to end this bare key
          keyName = this.data.substring(this.pos, i);
          this.pos = i;
          return keyName;
        }
      }
      if (this.strictAsciiBareKeys) {
        if (!(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || ((c >= '0') && (c <= '9')) || (c == '_')
            || (c == '-'))) {
          throw new TomlException("Forbidden character '" + toString(c) + "' in strict bare-key at line " + this.line);
        }
      } else if ((c <= ' ') || (c == '#') || (c == ';') || (c == '=') || (c == '.') || (c == '[') || (c == ']')) {// lenient
        // bare key
        throw new TomlException("Forbidden character '" + toString(c) + "' in lenient bare-key at line " + this.line);
      } // else continue reading
    }
    throw new TomlException("Invalid key/value pair at line " + this.line
        + " end of data reached before the value attached to the key was found");
  }

  private String nextLiteralString() {
    int index = this.data.indexOf('\'', this.pos);
    if (index == -1) {
      throw new TomlException("Invalid literal String at line " + this.line + ": it never ends");
    }

    String str = this.data.substring(this.pos, index);
    if (str.indexOf('\n') != -1) {
      throw new TomlException("Invalid literal String at line " + this.line + ": newlines are not allowed here");
    }

    this.pos = index + 1;
    return str;
  }

  private String nextLiteralMultilineString() {
    int index = this.data.indexOf("'''", this.pos);
    if (index == -1) {
      throw new TomlException("Invalid multiline literal String at line " + this.line + ": it never ends");
    }
    String str;
    if ((this.data.charAt(this.pos) == '\r') && (this.data.charAt(this.pos + 1) == '\n')) {// "\r\n"
                                                                                           // at the
                                                                                           // beginning
                                                                                           // of the
      // string
      str = this.data.substring(this.pos + 2, index);
      this.line++;
    } else if (this.data.charAt(this.pos) == '\n') {// '\n' at the beginning of the string
      str = this.data.substring(this.pos + 1, index);
      this.line++;
    } else {
      str = this.data.substring(this.pos, index);
    }
    for (int i = 0; i < str.length(); i++) {// count lines
      char c = str.charAt(i);
      if (c == '\n') {
        this.line++;
      }
    }
    this.pos = index + 3;// goes after the 3 quotes
    return str;
  }

  private String nextBasicString() {
    StringBuilder sb = new StringBuilder();
    boolean escape = false;
    while (hasNext()) {
      char c = next();
      if ((c == '\n') || (c == '\r')) {
        throw new TomlException("Invalid basic String at line " + this.line + ": newlines not allowed");
      }
      if (escape) {
        sb.append(unescape(c));
        escape = false;
      } else if (c == '\\') {
        escape = true;
      } else if (c == '"') {
        return sb.toString();
      } else {
        sb.append(c);
      }
    }
    throw new TomlException("Invalid basic String at line " + this.line + ": it nerver ends");
  }

  private String nextBasicMultilineString() {
    StringBuilder sb = new StringBuilder();
    boolean first = true, escape = false;
    while (hasNext()) {
      char c = next();
      if (first && ((c == '\r') || (c == '\n'))) {
        if ((c == '\r') && hasNext() && (this.data.charAt(this.pos) == '\n')) { // "\r\n"
          this.pos++;// so that it is NOT read by the next call to next()
        } else { // "\r\n"
          this.line++;
        }
        first = false;
        continue;
      }
      if (escape) {
        if ((c == '\r') || (c == '\n') || (c == ' ') || (c == '\t')) {
          if ((c == '\r') && hasNext() && (this.data.charAt(this.pos) == '\n')) { // "\r\n"
            this.pos++;
          } else if (c == '\n') {
            this.line++;
          }
          nextUseful(false);
          this.pos--;// so that it is read by the next call to next()
        } else {
          sb.append(unescape(c));
        }
        escape = false;
      } else if (c == '\\') {
        escape = true;
      } else if (c == '"') {
        if ((this.pos + 1) >= this.data.length()) {
          break;
        }
        if ((this.data.charAt(this.pos) == '"') && (this.data.charAt(this.pos + 1) == '"')) {
          this.pos += 2;
          return sb.toString();
        }
      } else if (c == '\n') {
        this.line++;
        sb.append(c);
      } else {
        sb.append(c);
      }
    }
    throw new TomlException("Invalid multiline basic String at line " + this.line + ": it never ends");
  }

  private char unescape(char c) {
    switch (c) {
      case 'b':
        return '\b';
      case 't':
        return '\t';
      case 'n':
        return '\n';
      case 'f':
        return '\f';
      case 'r':
        return '\r';
      case '"':
        return '"';
      case '\\':
        return '\\';
      case 'u': {// unicode uXXXX
        if ((this.data.length() - this.pos) < 5) {
          throw new TomlException("Invalid unicode code point at line " + this.line);
        }
        String unicode = this.data.substring(this.pos, this.pos + 4);
        this.pos += 4;
        try {
          int hexVal = Integer.parseInt(unicode, 16);
          return (char) hexVal;
        } catch (NumberFormatException ex) {
          throw new TomlException("Invalid unicode code point at line " + this.line, ex);
        }
      }
      case 'U': {// unicode UXXXXXXXX
        if ((this.data.length() - this.pos) < 9) {
          throw new TomlException("Invalid unicode code point at line " + this.line);
        }
        String unicode = this.data.substring(this.pos, this.pos + 8);
        this.pos += 8;
        try {
          int hexVal = Integer.parseInt(unicode, 16);
          return (char) hexVal;
        } catch (NumberFormatException ex) {
          throw new TomlException("Invalid unicode code point at line " + this.line, ex);
        }
      }
      default:
        throw new TomlException("Invalid escape sequence: \"\\" + c + "\" at line " + this.line);
    }
  }

  /**
   * Converts a char to a String. The char is escaped if needed.
   */
  private String toString(char c) {
    switch (c) {
      case '\b':
        return "\\b";
      case '\t':
        return "\\t";
      case '\n':
        return "\\n";
      case '\r':
        return "\\r";
      case '\f':
        return "\\f";
      default:
        return String.valueOf(c);
    }
  }

}