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

package it.smartio.util.http.postman;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import it.smartio.util.http.HttpMessage;
import it.smartio.util.http.HttpQueue;
import it.smartio.util.http.HttpRequest;
import it.smartio.util.http.HttpResource;
import it.smartio.util.http.HttpResponse;
import it.smartio.util.test.TestSuite;
import it.smartio.util.test.TestSuiteBuilder;

/**
 * The {@link PostmanParser} defines the constants used in the json.
 */
public class PostmanParser {

  private static final Pattern VARIABLE = Pattern.compile("\\{\\{([^}]+)\\}\\}");


  private static final String NAME             = "name";
  private static final String ITEM             = "item";

  private static final String KEY              = "key";
  private static final String VALUE            = "value";

  private static final String METHOD           = "method";
  private static final String HEADER           = "header";
  private static final String CODE             = "code";
  private static final String URL              = "url";
  private static final String HOST             = "host";
  private static final String PATH             = "path";
  private static final String QUERY            = "query";

  private static final String REQUEST          = "request";
  private static final String RESPONSE         = "response";
  private static final String BODY             = "body";
  private static final String ORIGINAL_REQUEST = "originalRequest";


  private static final String MODE           = "mode";
  private static final String MODE_RAW       = "raw";
  private static final String MODE_FORM_DATA = "formdata";

  private static final String AUTH           = "auth";
  private static final String AUTH_BASIC     = "basic";
  private static final String AUTH_USERNAME  = "username";
  private static final String AUTH_PASSWORD  = "password";


  private final Postman    postman;
  private final Properties variables;

  /**
   * Constructs an instance of {@link PostmanParser}.
   *
   * @param postman
   * @param variables
   */
  private PostmanParser(Postman postman, Properties variables) {
    this.postman = postman;
    this.variables = variables;
  }

  /**
   * Get the {@link Postman} builder instance.
   */
  protected final Postman builder() {
    return this.postman;
  }

  /**
   * Replaces the text with the environment variables.
   *
   * @param text
   */
  protected final String ENV(String text) {
    int offset = 0;
    StringBuilder builder = new StringBuilder();
    Matcher matcher = PostmanParser.VARIABLE.matcher(text);
    while (matcher.find()) {
      builder.append(text.substring(0, matcher.start()));
      String name = matcher.group(1);
      if (this.variables.containsKey(name)) {
        builder.append(this.variables.get(name));
      }
      offset = matcher.end();
    }
    builder.append(text.substring(offset));
    return builder.toString();
  }

  /**
   * Parse a Postman file for HTTP request queues.
   *
   * @param filename
   * @param properties
   */
  public static Postman parse(String filename, Properties properties) throws IOException {
    try (InputStream stream = new FileInputStream(filename)) {
      return PostmanParser.parse(stream, properties);
    }
  }

  /**
   * Parse a Postman file for HTTP request queues.
   *
   * @param stream
   * @param properties
   */
  public static Postman parse(InputStream stream, Properties properties) throws IOException {
    JsonReader parser = Json.createReader(stream);
    JsonObject object = parser.readObject();
    JsonArray items = object.getJsonArray(PostmanParser.ITEM);

    Postman postman = new Postman();
    PostmanParser parser2 = new PostmanParser(postman, properties);
    parser2.parseItems(items);
    parser2.parseAuth(object);
    return parser2.builder();
  }


  /**
   * Parses a single item from the file
   *
   * @param items
   */
  protected final void parseItems(JsonArray items) {
    for (int i = 0; i < items.size(); i++) {
      JsonObject item = items.getJsonObject(i);
      JsonArray children = item.getJsonArray(PostmanParser.ITEM);
      if (children == null) {
        parseItem(item);
      } else {
        parseItems(children);
      }
    }
  }

  /**
   * Parses a single item from the file
   *
   * @param item
   */
  protected final void parseItem(JsonObject item) {
    String name = item.getString(PostmanParser.NAME);
    JsonObject json = item.getJsonObject(PostmanParser.REQUEST);
    String method = json.getString(PostmanParser.METHOD);
    HttpResource uri = parseUri(json);

    PostmanRequest request = new PostmanRequest(name, method, uri);
    PostmanParser.parseHeaders(json, request);
    PostmanParser.parseContent(json, request);

    JsonArray responses = item.getJsonArray(PostmanParser.RESPONSE);
    if (responses != null) {
      for (int i = 0; i < responses.size(); i++) {
        JsonObject resp = responses.getJsonObject(i);
        int code = resp.getInt(PostmanParser.CODE);
        HttpResponse response = new HttpResponse(code);
        PostmanParser.parseHeaders(resp, response);
        if (resp.containsKey(PostmanParser.BODY) && !resp.isNull(PostmanParser.BODY)) {
          response.setContent(resp.getString(PostmanParser.BODY));
        }

        String name2 = resp.getString(PostmanParser.NAME);
        JsonObject json2 = resp.getJsonObject(PostmanParser.ORIGINAL_REQUEST);
        String method2 = json2.getString(PostmanParser.METHOD);
        HttpResource uri2 = parseUri(json2);
        HttpRequest req2 = new HttpRequest(name2, method2, uri2, response);
        PostmanParser.parseHeaders(json2, req2);
        PostmanParser.parseContent(json2, req2);
        request.addHttpRequest(req2);
      }
    }

    String cookie = request.getCookie();
    if (request.hasSetCookie()) {
      cookie = request.getSetCookie();
    }

    if (cookie != null) {
      HttpQueue queue = builder().getQueues().get(cookie);
      if (queue == null) {
        queue = new HttpQueue(cookie);
        builder().getQueues().put(cookie, queue);
      }
      queue.addRequest(request);
    }

    if (name != null) {
      builder().getRequests().put(name, request);
    }
  }

  /**
   * Parse the RAW URL from a Postman request.
   *
   * @param json
   */
  protected final HttpResource parseUri(JsonObject json) {
    JsonObject url = json.getJsonObject(PostmanParser.URL);
    String raw = ENV(url.getString(PostmanParser.MODE_RAW));

    HttpResource uri = new HttpResource(raw);
    // uri.setProtocol(url.getString(Postman.Protocol));
    JsonArray host = url.getJsonArray(PostmanParser.HOST);
    if ((host != null) && !host.isEmpty()) {
      uri.setHost(ENV(host.getString(0)));
    }
    // uri.setPort(url.getString(Postman.Port));
    JsonArray path = url.getJsonArray(PostmanParser.PATH);
    if (path != null) {
      List<String> array = new ArrayList<>();
      for (int i = 0; i < path.size(); i++) {
        array.add(ENV(path.getString(i)));
      }
      uri.setPath(String.join("/", array));
    }
    JsonArray query = url.getJsonArray(PostmanParser.QUERY);
    if (query != null) {
      for (int i = 0; i < query.size(); i++) {
        JsonObject value = query.getJsonObject(i);
        if (!value.isNull(PostmanParser.KEY)) {
          String text = value.isNull(PostmanParser.VALUE) ? null : ENV(value.getString(PostmanParser.VALUE));
          uri.addQuery(value.getString(PostmanParser.KEY), text);
        }
      }
    }
    return uri;
  }

  /**
   * Parse the HTTP headers from a Postman request/response.
   *
   * @param json
   * @param message
   */
  private static void parseHeaders(JsonObject json, HttpMessage message) {
    JsonArray headers = json.getJsonArray(PostmanParser.HEADER);
    for (int j = 0; j < headers.size(); j++) {
      JsonObject kv = headers.getJsonObject(j);
      message.setHeader(kv.getString(PostmanParser.KEY), kv.getString(PostmanParser.VALUE));
    }
  }

  /**
   * Parse the HTTP headers from a Postman request/response.
   *
   * @param json
   * @param message
   */
  private static void parseContent(JsonObject json, HttpMessage message) {
    JsonObject body = json.getJsonObject(PostmanParser.BODY);
    if ((body != null) && !body.isEmpty()) {
      switch (body.getString(PostmanParser.MODE)) {
        case PostmanParser.MODE_RAW:
          message.setContent(body.getString(PostmanParser.MODE_RAW));
          break;

        case PostmanParser.MODE_FORM_DATA:
          // TODO
          break;

        default:
          break;
      }
    }
  }

  /**
   * Parses a the authenticator from the postman file. Currently only the BASIC authentication is
   * supported.
   *
   * @param json
   */
  protected final void parseAuth(JsonObject json) {
    if (json.containsKey(PostmanParser.AUTH)) {
      JsonObject auth = json.getJsonObject(PostmanParser.AUTH);
      if (auth.containsKey(PostmanParser.AUTH_BASIC)) {
        JsonArray values = auth.getJsonArray(PostmanParser.AUTH_BASIC);
        for (int index = 0; index < values.size(); index++) {
          JsonObject entry = values.getJsonObject(index);
          switch (entry.getString(PostmanParser.KEY)) {
            case AUTH_USERNAME:
              builder().setUsername(ENV(entry.getString(PostmanParser.VALUE)));
              break;
            case AUTH_PASSWORD:
              builder().setPassword(ENV(entry.getString(PostmanParser.VALUE)));
              break;
            default:
              break;
          }
        }
      }
    }
  }

  /**
   * Loads a postman file as {@link TestSuite}.
   *
   * @param file
   * @param properties
   */
  public static TestSuite loadSuite(File file, Properties properties) throws IOException {
    TestSuiteBuilder builder = new TestSuiteBuilder(file.getName());
    properties.keySet().forEach(k -> builder.addProperty("" + k, (String) properties.get(k)));

    try (InputStream stream = new FileInputStream(file)) {
      Postman p = PostmanParser.parse(stream, properties);
      for (PostmanRequest postman : p.getRequests().values()) {
        builder.setClassname(postman.getName());
        for (HttpRequest request : postman.getHttpRequests()) {
          builder.addTestCase(request.getName(), r -> p.test(r, request));
        }
      }
    }
    return builder.build();
  }
}
