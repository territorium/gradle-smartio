/*
 * Copyright (c) 2001-2022 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.bz.it/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package it.smartio.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;


/**
 * The {@link HttpRequestHandler} class.
 */
public class HttpRequestHandler {

  /**
   * Get the response from the named postman request, replacing the path
   *
   * @param request
   * @param context
   */
  public final HttpResponse handleRequest(HttpRequest request, HttpContext context) throws IOException {
    HttpURLConnection conn = getConnection(request, context);
    if (Http.METHOD_PUT.equalsIgnoreCase(request.getMethod()) || Http.METHOD_POST.equalsIgnoreCase(request.getMethod())
        || Http.METHOD_PATCH.equalsIgnoreCase(request.getMethod())) {
      HttpRequestHandler.writeData(conn, request.getContent());
    }

    HttpResponse response = new HttpResponse(conn.getResponseCode());
    conn.getHeaderFields().keySet().stream().filter(n -> n != null)
        .forEach(n -> response.setHeader(n, conn.getHeaderField(n)));
    if (response.getStatusCode() < 400) {
      response.setContent(HttpRequestHandler.readStream(conn.getInputStream()));
    }
    conn.disconnect();
    return response;
  }

  /**
   * Create an {@link HttpURLConnection} using the {@link HttpRequest}..
   *
   * @param request
   * @param context
   */
  protected final HttpURLConnection getConnection(HttpRequest request, HttpContext context) throws IOException {
    String resource = request.getResource().getEncoded();
    HttpURLConnection conn = (HttpURLConnection) new URL(resource).openConnection();

    for (String key : request.getHeaderNames()) {
      conn.setRequestProperty(key, request.getHeader(key));
    }
    if (context.getSessionId() != null) {
      conn.setRequestProperty(Http.COOKIE, "JSESSIONID=" + context.getSessionId());
    }

    String auth = String.format("%s:%s", context.getUsername(), context.getPassword());
    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
    conn.setRequestProperty(Http.AUTHORIZATION, "BASIC " + new String(encodedAuth));

    conn.setRequestMethod(request.getMethod());
    if (Http.METHOD_PATCH.equalsIgnoreCase(request.getMethod())) {
      conn.setRequestProperty(Http.MethodOverride, Http.METHOD_PATCH);
      conn.setRequestMethod(Http.METHOD_POST);
    }
    return conn;
  }

  /**
   * Write the body to the {@link HttpURLConnection}.
   *
   * @param conn
   * @param body
   */
  protected static final void writeData(HttpURLConnection conn, String body) throws IOException {
    conn.setDoOutput(true);
    try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
      writer.write(body);
    }
  }

  /**
   * Write the body to the {@link HttpURLConnection}.
   *
   * @param conn
   * @param body
   * @param file
   */
  protected static final void writeFormData(HttpURLConnection conn, String body, String file) throws IOException {
    if (file == null) {
      file = "file" + Integer.toHexString(new Random().nextInt()) + ".txt";
    }
    String boundary = "----BoundaryFbqcBQHc7HMjBQkN";

    conn.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", boundary));
    conn.setDoOutput(true);

    try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
      writer.write(String.format("%s\r\n", boundary));
      writer.write(String.format("Content-Disposition: form-data; name=\"\"; filename=\"%s\"\r\n", file));
      writer.write("Content-Type: text/plain\r\n");
      writer.write("\r\n");
      writer.write("Hello World!\n");
      writer.write("\r\n");
      writer.write(String.format("%s--\r\n", boundary));
    }
  }

  /**
   * Reads an {@link InputStream} and return the String.
   *
   * @param istream
   * @throws IOException
   */
  private static final String readStream(InputStream istream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
    StringBuilder builder = new StringBuilder();
    String output;
    while ((output = reader.readLine()) != null) {
      builder.append(output);
    }
    return builder.toString();
  }
}
