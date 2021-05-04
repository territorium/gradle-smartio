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

import java.util.HashMap;
import java.util.Map;

import it.smartio.docs.builder.CodeBuilder;
import it.smartio.docs.builder.CodeParser;
import it.smartio.docs.builder.SectionBuilder;

/**
 * The {@link CodeFactory} implements a tokenizer for a specific language.
 */
public class CodeFactory implements CodeParser {

  private final Map<String, CodeParser> parsers = new HashMap<>();

  /**
   * Constructs an instance of {@link CodeFactory}.
   */
  public CodeFactory() {
    this.parsers.put("ini", new CodeParserIni());
    this.parsers.put("conf", new CodeParserConf());
    this.parsers.put("yaml", new CodeParserYaml());
    this.parsers.put("shell", new CodeParserShell());

    this.parsers.put("meta", new CodeParserMeta());
    this.parsers.put("java", new CodeParserJava());
    this.parsers.put("c", new CodeParserCpp());
    this.parsers.put("cpp", new CodeParserCpp());
    this.parsers.put("c++", new CodeParserCpp());
    this.parsers.put("sql", new CodeParserSql());
    this.parsers.put("oql", new CodeParserSql());

    this.parsers.put("xml", new CodeParserXml());
    this.parsers.put("json", new CodeParserJson());
    this.parsers.put("api", new CodeParserApi());
  }

  /**
   * Generates the code text
   *
   * @param node
   */
  @Override
  public final void generate(String text, CodeBuilder builder) {
    builder.addText(text);
  }

  /**
   * Creates a {@link CodeFactory} for a specific language.
   *
   * @param name
   * @param section
   */
  public final void generate(String name, String text, SectionBuilder builder) {
    String key = name.toLowerCase();
    CodeParser parser = this.parsers.containsKey(key) ? this.parsers.get(key) : this;

    CodeBuilder styled = builder.addCode();
    styled.setStyled(true);
    parser.generate(text, styled);
  }
}
