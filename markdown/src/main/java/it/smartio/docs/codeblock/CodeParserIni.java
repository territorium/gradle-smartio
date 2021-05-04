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
 * The {@link CodeParserIni} class.
 */
class CodeParserIni implements CodeParser {

  private static final String  PATTERN_TEXT = "^(?:(\\[[^;]+)|([^=]+)(=[^;]*))?(;.+)?$";
  private static final Pattern PATTERN      = Pattern.compile(CodeParserIni.PATTERN_TEXT, Pattern.CASE_INSENSITIVE);

  /**
   * Generates the code text
   *
   * @param node
   */
  @Override
  public final void generate(String text, CodeBuilder builder) {
    for (String line : text.split("\\n")) {
      Matcher matcher = CodeParserIni.PATTERN.matcher(line);
      if (matcher.find()) {
        if (matcher.group(1) != null) { // Section
          builder.addInline(matcher.group(1)).setBold().setColor(CodeToken.SECTION.COLOR);
        } else if (matcher.group(2) != null) { // Parameter
          builder.addInline(matcher.group(2)).setColor(CodeToken.PARAMETER.COLOR);
          builder.addInline(matcher.group(3)).setColor(CodeToken.VALUE.COLOR);
        }

        if (matcher.group(4) != null) { // Comment
          builder.addInline(matcher.group(4)).setItalic().setColor(CodeToken.COMMENT.COLOR);
        }
      } else {
        builder.addText(line);
      }
      builder.addText("\n");
    }
  }
}
