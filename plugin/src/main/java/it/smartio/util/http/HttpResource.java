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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link HttpResource} defines an HTTP URI..
 */
public class HttpResource {

  private final String raw;

  private String       protocol;
  private String       host;
  private String       port;
  private String       path;


  private final Map<String, String> query = new HashMap<>();

  /**
   * Constructs an instance of {@link HttpResource}.
   *
   * @param raw
   */
  public HttpResource(String raw) {
    this.raw = raw;
  }

  /**
   * Gets the raw {@link URI}.
   */
  public final String getRaw() {
    return this.raw;
  }

  /**
   * Gets the encoded URI.
   */
  public final String getUri() {
    String resource = this.protocol == null ? "" : this.protocol + "://";
    resource += this.host;
    resource += this.port == null ? "" : ":" + this.port;
    resource += this.path == null ? "" : "/" + this.path;
    return resource;
  }

  /**
   * Gets the Query string
   */
  public final String getQuery() {
    if (this.query.isEmpty()) {
      return null;
    }

    List<String> params = new ArrayList<>();
    for (String name : this.query.keySet()) {
      if (this.query.get(name) == null) {
        params.add(name);
      } else {
        params.add(String.format("%s=%s", name, Http.encode(this.query.get(name))));
      }
    }
    return String.join("&", params);
  }

  /**
   * Gets the raw {@link URI} replacing the placeholder.
   */
  public final String getEncoded() {
    String uri = getUri();
    String query = getQuery();
    return (query == null) ? uri : String.format("%s?%s", uri, query);
  }

  /**
   * Sets the protocol.
   *
   * @param protocol
   */
  public final void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Sets the host.
   *
   * @param host
   */
  public final void setHost(String host) {
    this.host = host;
  }

  /**
   * Sets the port.
   *
   * @param port
   */
  public final void setPort(String port) {
    this.port = port;
  }

  /**
   * Sets the path.
   *
   * @param path
   */
  public final void setPath(String path) {
    this.path = path;
  }

  /**
   * Puts a query value on the resource.
   *
   * @param name
   * @param value
   */
  public final void addQuery(String name, String value) {
    this.query.put(name, value);
  }
}
