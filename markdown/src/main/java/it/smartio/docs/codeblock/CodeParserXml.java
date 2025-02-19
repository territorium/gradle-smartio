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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.docs.builder.CodeBuilder;
import it.smartio.docs.builder.CodeParser;

/**
 * The {@link CodeParserXml} class.
 */
class CodeParserXml implements CodeParser {

  private static final String  PATTERN_TEXT = "(<?xml .+?>)|(<\\w+|\\s*/?>|</\\w+>)|([^\\s]+=)(\"[^\"]+\")";
  private static final Pattern PATTERN      =
      Pattern.compile(CodeParserXml.PATTERN_TEXT, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  /**
   * Generates the code text
   *
   * @param node
   */
  @Override
  public final void generate(String text, CodeBuilder builder) {
    int index = 0;
    Matcher matcher = CodeParserXml.PATTERN.matcher(text);
    while (matcher.find()) {
      if (matcher.group(1) != null) { // Comment
        builder.addInline("<?" + matcher.group(1)).setItalic().setColor(CodeToken.COMMENT.COLOR);
        index = matcher.end(1);
      }

      if (matcher.group(2) != null) {
        if (matcher.start(2) > index) {
          builder.addInline(text.substring(index, matcher.start(2))).setBold().setColor(CodeToken.TEXT.COLOR);
        }
        builder.addInline(matcher.group(2)).setBold().setColor(CodeToken.KEYWORD.COLOR);
        index = matcher.end(2);
      }

      if (matcher.group(3) != null) {
        if (matcher.start(3) > index) {
          builder.addInline(text.substring(index, matcher.start(3))).setBold().setColor(CodeToken.TEXT.COLOR);
        }

        builder.addInline(matcher.group(3)).setBold().setColor(CodeToken.PARAMETER.COLOR);
        builder.addInline(matcher.group(4)).setBold().setColor(CodeToken.VALUE.COLOR);
        index = matcher.end(4);
      }
    }

    if (index < text.length()) { // Text
      builder.addInline(text.substring(index)).setBold().setColor(CodeToken.TEXT.COLOR);
    }
  }
}
