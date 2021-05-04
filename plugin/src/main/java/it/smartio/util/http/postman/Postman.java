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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.smartio.util.http.HttpContext;
import it.smartio.util.http.HttpQueue;
import it.smartio.util.http.HttpRequest;
import it.smartio.util.http.HttpRequestHandler;
import it.smartio.util.http.HttpResponse;
import it.smartio.util.test.TestResult;

/**
 * The {@link Postman} class.
 */
public class Postman extends HttpContext {

  private final Map<String, HttpQueue>      queues   = new HashMap<>();
  private final Map<String, PostmanRequest> requests = new HashMap<>();
  private final HttpRequestHandler          handler  = new HttpRequestHandler();

  /**
   * Constructs an instance of {@link Postman}.
   */
  Postman() {}

  /**
   * Gets the {@link HttpQueue}'s
   */
  public final Map<String, HttpQueue> getQueues() {
    return this.queues;
  }

  /**
   * Gets the {@link HttpRequest}'s.
   */
  public final Map<String, PostmanRequest> getRequests() {
    return this.requests;
  }

  /**
   * Assert a {@link HttpResponse} against the expected response.
   *
   * @param result
   * @param request
   */
  public final void test(TestResult result, HttpRequest request) throws IOException {
    HttpResponse expected = request.getResponse();
    HttpResponse response = handler.handleRequest(request, this);

    result.assertInt(expected.getStatusCode(), response.getStatusCode(), "Invalid HTTP Status Code");

    for (String name : expected.getHeaderNames()) {
      result.assertString(expected.getHeader(name), response.getHeader(name), String.format("Http-Header '%s'", name));
    }

    if ((expected.getContent() != null) && (response.getContent() != null) && result.getFailures().isEmpty()) {
      if ("application/json".equals(expected.getHeader("Content-Type"))) {
        result.assertJson(expected.getContent(), response.getContent());
      }
    }
  }
}
