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

/**
 * The {@link HttpRequest} class.
 */
public class HttpRequest extends HttpMessage {

  private final String       name;
  private final String       method;
  private final HttpResource resource;
  private final HttpResponse response;

  /**
   * Constructs an instance of {@link HttpRequest}.
   *
   * @param name
   * @param method
   * @param resource
   */
  public HttpRequest(String name, String method, HttpResource resource) {
    this(name, method, resource, null);
  }

  /**
   * Constructs an instance of {@link HttpRequest}.
   *
   * @param name
   * @param method
   * @param resource
   * @param response
   */
  public HttpRequest(String name, String method, HttpResource resource, HttpResponse response) {
    this.name = name;
    this.method = method;
    this.resource = resource;
    this.response = response;
  }

  /**
   * Get the request name.
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Get the HTTP method.
   */
  public final String getMethod() {
    return this.method;
  }

  /**
   * Get the Resource URI.
   */
  public final HttpResource getResource() {
    return this.resource;
  }

  /**
   * Get the first {@link HttpResponse} or <code>null</code>.
   */
  public final HttpResponse getResponse() {
    return this.response;
  }

  /**
   * Get the Cookie value.
   */
  public final String getCookie() {
    return Http.getSessionId(getHeader("cookie"));
  }

  /**
   * <code>true</code> if the first response contains a Set-Cookie.
   */
  public final boolean hasSetCookie() {
    return getResponse() == null ? false : getResponse().hasSetCookie();
  }

  /**
   * Get the Set-Cookie value
   */
  public final String getSetCookie() {
    return getResponse() == null ? null : getResponse().getSetCookie();
  }
}
