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

package it.smartio.util.http.postman;

import java.util.ArrayList;
import java.util.List;

import it.smartio.util.http.HttpRequest;
import it.smartio.util.http.HttpResource;


/**
 * The {@link PostmanRequest} class.
 */
public class PostmanRequest extends HttpRequest {

  private final List<HttpRequest> requests = new ArrayList<>();

  /**
   * Constructs an instance of {@link PostmanRequest}.
   *
   * @param name
   * @param method
   * @param resource
   */
  public PostmanRequest(String name, String method, HttpResource resource) {
    super(name, method, resource);
  }

  /**
   * Get an Iterable over the {@link HttpRequest}'s.
   */
  public final List<HttpRequest> getHttpRequests() {
    return this.requests;
  }

  /**
   * Add the {@link HttpRequest}'s.
   *
   * @param request
   */
  public final void addHttpRequest(HttpRequest request) {
    this.requests.add(request);
  }
}
