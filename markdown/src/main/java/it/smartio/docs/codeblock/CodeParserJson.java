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
 * The {@link CodeParserJson} class.
 */
class CodeParserJson implements CodeParser {

  private static final String  PATTERN_TEXT = "(\"[^\"]+\":)|(\"[^\"]+\")|([0-9.]+|true|false)";
  private static final Pattern PATTERN      = Pattern.compile(CodeParserJson.PATTERN_TEXT, Pattern.CASE_INSENSITIVE);

  /**
   * Generates the code text
   *
   * @param node
   */
  @Override
  public final void generate(String text, CodeBuilder builder) {
    int offset = 0;
    Matcher matcher = CodeParserJson.PATTERN.matcher(text);
    while (matcher.find()) {
      if (matcher.group(1) != null) {
        if (matcher.start(1) > offset) {
          builder.addText(text.substring(offset, matcher.start(1)));
        }

        builder.addInline(matcher.group(1)).setBold().setColor(CodeToken.JSON_NAME.COLOR);
        offset = matcher.end(1);
      } else if (matcher.group(2) != null) {
        if (matcher.start(2) > offset) {
          builder.addText(text.substring(offset, matcher.start(2)));
        }

        builder.addInline(matcher.group(2)).setBold().setColor(CodeToken.JSON_TEXT.COLOR);
        offset = matcher.end(2);
      } else if (matcher.group(3) != null) {
        if (matcher.start(3) > offset) {
          builder.addText(text.substring(offset, matcher.start(3)));
        }

        builder.addInline(matcher.group(3)).setBold().setColor(CodeToken.JSON_VALUE.COLOR);
        offset = matcher.end(3);
      }
    }
    if (offset < text.length()) {
      builder.addText(text.substring(offset));
    }
  }
}
