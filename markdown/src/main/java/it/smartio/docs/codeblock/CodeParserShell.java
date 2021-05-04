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
 * The {@link CodeParserShell} class.
 */
class CodeParserShell implements CodeParser {

  private static final Pattern PATTERN      = Pattern.compile("^(#.+)|([^\\s]+)(.+)?$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PATTERN_ARGS = Pattern.compile("(\\s+-[^\\s]+)?(\\s+[^\\s]+)", Pattern.CASE_INSENSITIVE);

  /**
   * Generates the code text
   *
   * @param node
   */
  @Override
  public final void generate(String text, CodeBuilder builder) {
    builder.setFontSize("10pt");

    for (String line : text.split("\\n")) {
      Matcher matcher = CodeParserShell.PATTERN.matcher(line);
      if (matcher.find()) {
        if (matcher.group(1) != null) { // Comment
          builder.addInline(line).setItalic().setColor(CodeToken.COMMENT.COLOR);
        }

        if (matcher.group(2) != null) { // Command
          builder.addInline(matcher.group(2)).setBold().setColor(CodeToken.PARAMETER.COLOR);

          String arguments = matcher.group(3);
          if (arguments == null) {
            continue;
          }

          Matcher args = CodeParserShell.PATTERN_ARGS.matcher(arguments);
          while (args.find()) {// Arguments
            if (args.group(1) != null) {
              builder.addInline(args.group(1)).setColor(CodeToken.VALUE.COLOR);
            }
            builder.addText(args.group(2));
          }
        }
      } else {
        builder.addText(line);
      }
      builder.addText("\n");
    }
  }
}
