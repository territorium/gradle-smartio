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
import it.smartio.docs.builder.ParagraphBuilder;
import it.smartio.docs.builder.TableBuilder;
import it.smartio.docs.builder.TableBuilder.RowBuilder;

/**
 * The {@link CodeParserApi} class.
 */
class CodeParserApi implements CodeParser {

  private static List<String> METHODS   = Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE");
  private static List<String> METHOD_FG = Arrays.asList("#aaaaaa", "#61affe", "#49cc90", "#fca130", "#f13e3e");
  private static List<String> METHOD_BG = Arrays.asList("#eeeeee", "#c7e3ff", "#bfedd8", "#fdcf96", "#fbcbcb");


  private static final String  PATTERN_TEXT  = "^(\\s*)([\\w-]+):\\s*(.+)?$";
  private static final String  PATTERN_IMAGE = "!\\[\\]\\(([^\\)]+)\\)(?:\\{width=(.+)\\})?";
  private static final Pattern PATTERN       = Pattern.compile(CodeParserApi.PATTERN_TEXT, Pattern.CASE_INSENSITIVE);
  private static final Pattern PATTERN2      = Pattern.compile(CodeParserApi.PATTERN_IMAGE, Pattern.CASE_INSENSITIVE);

  protected final String getColor(String method) {
    int index = CodeParserApi.METHODS.indexOf(method.toUpperCase());
    return CodeParserApi.METHOD_FG.get(index < 0 ? 0 : index);
  }

  protected final String getBackground(String method) {
    int index = CodeParserApi.METHODS.indexOf(method.toUpperCase());
    return CodeParserApi.METHOD_BG.get(index < 0 ? 0 : index);
  }

  protected final String getStatusBackground(String status) {
    int value = Integer.parseInt(status);
    if (value < 200) {
      return "#ffffff";
    } else if (value < 300) {
      return "#00c853";
    } else if (value < 400) {
      return "#fac853";
    } else if (value < 500) {
      return "#fa2d2d";
    }
    return "#aa2d2d";
  }

  /**
   * Generates the code text
   *
   * @param node
   */
  @Override
  public final void generate(String text, CodeBuilder builder) {
    TableBuilder table = builder.addVirtualTable();
    String method = "";
    String content = null;
    String contentType = "application/json";

    table.addColumn(1, "left");
    table.addColumn(4, "left");
    table.addBody();

    for (String line : text.split("\\n")) {
      Matcher matcher = CodeParserApi.PATTERN.matcher(line);
      if (matcher.find()) {
        int intend = matcher.group(1).length();
        String name = matcher.group(2);
        String value = matcher.group(3);

        if (content != null) {
          processContent(table, content, contentType);
          content = null;
          contentType = "application/json";
        }

        if ("content".equalsIgnoreCase(name)) {
          content = value;
        } else if (intend == 0) {
          if ("method".equalsIgnoreCase(name)) {
            method = value.toUpperCase();
          } else if ("path".equalsIgnoreCase(name)) {
            RowBuilder row = table.addRow();
            ParagraphBuilder cell = row.addCell(1, 2).getContent().setPadding("0pt", "3pt");
            cell.addInline().setPadding("15pt", "5pt").setBackground(getColor(method)).setBold()
                .addContent(method.toUpperCase());
            cell.addInline().setPadding("5pt", "5pt").setBold().addContent(value);
          } else if ("response".equalsIgnoreCase(name) && (value != null)) {
            String background = getStatusBackground(value);
            RowBuilder row = table.addRow();
            row.addCell(1, 2).getContent().addInline().setPadding("5pt", "2pt").setBackground(background).setBold()
                .addContent(name + " " + value);
          } else {
            RowBuilder row = table.addRow();
            row.addCell(1, 2).getContent().addInline().setPadding("5pt", "2pt").setBackground("#ffffff").setBold()
                .addContent(name);
          }
        } else if (intend > 0) {
          if ("content-type".equalsIgnoreCase(name)) {
            contentType = value.trim();
          }

          if ("content".equalsIgnoreCase(name)) {
            content = value;
          } else if (value != null) {
            RowBuilder row = table.addRow();
            row.addCell(1, 1).getContent().setPadding("5pt", "2pt").addInline().setPadding("15pt", "5pt").setBold()
                .addContent(name);
            row.addCell(1, 1).getContent().setPadding("5pt", "2pt").addInline().setPadding("15pt", "5pt")
                .addContent(value);
          }
        }
      } else if (content != null) {
        content += "\n" + line;
      }
    }

    if (content != null) {
      processContent(table, content, contentType);
      content = null;
      contentType = "application/json";
    }

    table.setBorderColor(getColor(method));
    table.setBackgroundColor(getBackground(method));
  }

  protected final void processContent(TableBuilder table, String content, String contentType) {
    RowBuilder row = table.addRow();
    ParagraphBuilder paragraph = row.addCell(1, 2).getContent().setPadding("0pt", "0pt", "2pt", "0pt");

    if ("application/json".equalsIgnoreCase(contentType.trim())) {
      CodeBuilder builder = paragraph.addCode();
      builder.setInline(true);
      builder.setBackground("#fff");
      new CodeParserJson().generate(content, builder);
    } else if ("application/xml".equalsIgnoreCase(contentType.trim())) {
      CodeBuilder builder = paragraph.addCode();
      builder.setInline(true);
      builder.setBackground("#fff");
      new CodeParserXml().generate(content, builder);
    } else if (contentType.trim().toLowerCase().startsWith("image/")) {
      Matcher matcher = CodeParserApi.PATTERN2.matcher(content.trim());
      if (matcher.find()) {
        String source = matcher.group(1);
        String width = matcher.group(2);
        paragraph.addImage(source, null, null, width, null);
      } else {
        paragraph.addContent(content);
      }
    } else {
      paragraph.addCode().addContent(content);
    }
  }
}
