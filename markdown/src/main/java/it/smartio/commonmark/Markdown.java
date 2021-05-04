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

package it.smartio.commonmark;

import java.io.IOException;
import java.util.Arrays;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;

import it.smartio.commonmark.alerts.AlertExtension;
import it.smartio.commonmark.images.ImageExtension;
import it.smartio.commonmark.markers.MarkerExtension;
import it.smartio.commonmark.tables.TableExtension;

/**
 * The {@link Markdown} implements a reader based on Markdown. The Reader starts from a file an
 * includes referenced files to create a single huge Markdown file.
 */
public interface Markdown {

  /**
   * Creates an instance of {@link Parser}.
   */
  static Parser newInstance() throws IOException {
    return Markdown.newInstance(ImageExtension.create(), TableExtension.create(), AlertExtension.create(),
        MarkerExtension.create());
  }

  /**
   * Creates an instance of {@link Parser}.
   */
  static Parser newInstance(Extension... extensions) throws IOException {
    return Parser.builder().extensions(Arrays.asList(extensions)).build();
  }
}
