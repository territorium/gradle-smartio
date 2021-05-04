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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link Http} defines HTTP constants.
 */
public interface Http {

  String METHOD_GET     = "GET";
  String METHOD_POST    = "POST";
  String METHOD_PUT     = "PUT";
  String METHOD_DELETE  = "DELETE";
  String METHOD_PATCH   = "PATCH";
  String METHOD_HEAD    = "HEAD";
  String METHOD_OPTIONS = "OPTIONS";


  String  AUTHORIZATION = "Authorization";
  String  COOKIE        = "Cookie";
  String  SET_COOKIE    = "Set-Cookie";

  Pattern SESSIONID     = Pattern.compile("^JSESSIONID=([a-f0-9]+)", Pattern.CASE_INSENSITIVE);


  String MethodOverride = "X-HTTP-Method-Override";


  /**
   * Get the session id from a header cookie or set-cookie.
   *
   * @param header
   */
  static String getSessionId(String header) {
    if (header == null) {
      return null;
    }

    Matcher matcher = Http.SESSIONID.matcher(header);
    return matcher.find() ? matcher.group(1) : null;
  }

  /**
   * Replaces the text with the provided properties.
   *
   * @param text
   */
  static String encode(String text) {
    try {
      return URLEncoder.encode(text, "UTF-8");
    } catch (UnsupportedEncodingException e) {}
    return text;
  }
}

