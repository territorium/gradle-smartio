/**
 * Copyright (C) 2016 Matthieu Brouillard [http://oss.brouillard.fr/jgitver]
 * (matthieu@brouillard.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package it.smartio.commonmark.alerts;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;

public class AlertExtension implements Parser.ParserExtension {

  private AlertExtension() {}

  public static Extension create() {
    return new AlertExtension();
  }

  @Override
  public void extend(org.commonmark.parser.Parser.Builder parserBuilder) {
    parserBuilder.customBlockParserFactory(new AlertBlockParser.Factory());
  }
}
