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

package it.smartio.util.http;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link HttpMessage} class.
 */
public abstract class HttpMessage {

  private String                    content;
  private final Map<String, String> headers = new HashMap<>();

  /**
   * Gets the name of a header.
   */
  public final Iterable<String> getHeaderNames() {
    return this.headers.keySet();
  }

  /**
   * Return <code>true</code> if the header is set.
   *
   * @param name
   */
  public final boolean hasHeader(String name) {
    return this.headers.containsKey(name.toLowerCase());
  }

  /**
   * Gets the name of a header.
   *
   * @param name
   */
  public final String getHeader(String name) {
    return this.headers.get(name.toLowerCase());
  }

  /**
   * Sets a header value.
   *
   * @param name
   * @param value
   */
  public final void setHeader(String name, String value) {
    this.headers.put(name.toLowerCase(), value);
  }

  /**
   * Get the body content.
   */
  public final String getContent() {
    return this.content;
  }

  /**
   * Set the body content.
   *
   * @param content
   */
  public final void setContent(String content) {
    this.content = content;
  }
}
