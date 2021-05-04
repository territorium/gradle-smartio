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

package it.smartio.docs.codeblock;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.docs.builder.CodeBuilder;
import it.smartio.docs.builder.CodeParser;

/**
 * The {@link CodeParserSql} class.
 */
class CodeParserSql implements CodeParser {

  private static final List<String> KEYWORDS =
      Arrays.asList("SELECT", "FROM", "WHERE", "DISTINCT", "ON", "AS", "USING", "WHERE", "GROUP", "BY", "ORDER",
          "LIMIT", "OFFSET", "CREATE", "INSERT", "UPDATE", "DELETE", "INTO", "SET", "VALUES", "IMPORT", "EXPORT",
          "WITH", "DPI", "SIZE", "ANGLE", "LAYER", "SYNC", "BINARY", "SCHEMA", "UNCAST", "IS", "NOT", "NULL");


  private static final Pattern LANGUAGE =
      Pattern.compile(String.format("(%s)", String.join("|", CodeParserSql.KEYWORDS)), Pattern.CASE_INSENSITIVE);

  /**
   * Generates the code text
   *
   * @param node
   */
  @Override
  public final void generate(String text, CodeBuilder builder) {
    for (String line : text.split("\\n")) {
      if (line.startsWith("--")) {
        builder.addInline(line + "\n").setItalic().setColor(CodeToken.COMMENT.COLOR);
      } else {
        Matcher matcher = CodeParserSql.LANGUAGE.matcher(line);
        int offset = 0;
        while (matcher.find()) {
          if (matcher.start() > offset) {
            builder.addText(line.substring(offset, matcher.start()));
          }
          builder.addInline(matcher.group(1)).setBold().setColor(CodeToken.KEYWORD.COLOR);
          offset = matcher.end();
        }
        builder.addText(line.substring(offset) + "\n");
      }
    }
  }
}
