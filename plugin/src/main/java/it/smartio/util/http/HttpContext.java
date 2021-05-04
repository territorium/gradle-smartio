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

/**
 * The {@link HttpContext} class.
 */
public class HttpContext {

  private String username;
  private String password;
  private String sessionId;

  /**
   * Gets the user name.
   */
  public final String getUsername() {
    return this.username;
  }

  /**
   * Gets the password.
   */
  public final String getPassword() {
    return this.password;
  }

  /**
   * Gets the session id.
   */
  public final String getSessionId() {
    return this.sessionId;
  }

  /**
   * Sets the user name.
   */
  public final void setUsername(String username) {
    this.username = username;
  }

  /**
   * Sets the password.
   */
  public final void setPassword(String password) {
    this.password = password;
  }

  /**
   * Sets the session id.
   */
  public final void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }
}
