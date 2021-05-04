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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The {@link HttpQueue} class.
 */
public class HttpQueue implements Iterable<HttpRequest> {

  private final String            sessionId;
  private final List<HttpRequest> requests = new ArrayList<>();

  /**
   * Constructs an instance of {@link HttpQueue}.
   *
   * @param cookie
   */
  public HttpQueue(String cookie) {
    this.sessionId = cookie;
  }

  /**
   * Get the related session id.
   */
  public final String getSessionId() {
    return this.sessionId;
  }

  /**
   * <code>true</code> if the {@link HttpQueue} defines a Set-Cookie on its first request.
   */
  public final boolean startsWithSetCookie() {
    return !this.requests.isEmpty() && this.requests.get(0).hasSetCookie();
  }


  /**
   * Get the number of {@link HttpRequest}.
   */
  public final int getNumRequests() {
    return this.requests.size();
  }

  /**
   * Get the {@link HttpRequest} at index.
   */
  public final HttpRequest getRequest(int index) {
    return (index < 0) || (index >= getNumRequests()) ? null : this.requests.get(index);
  }

  /**
   * Add the {@link HttpRequest}'s.
   *
   * @param request
   */
  public final void addRequest(HttpRequest request) {
    this.requests.add(request);
  }

  /**
   * Gets an iterable over {@link HttpRequest}'s.
   */
  @Override
  public final Iterator<HttpRequest> iterator() {
    return this.requests.iterator();
  }
}
