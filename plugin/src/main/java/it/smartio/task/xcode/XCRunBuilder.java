/*
 * Copyright (c) 2001-2021 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.task.xcode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.smartio.common.task.process.ProcessRequest;
import it.smartio.common.task.process.ProcessRequestBuilder;


/**
 * The {@link XCRunBuilder} class.
 */
public class XCRunBuilder extends ProcessRequestBuilder {

  private final File file;

  private String     apiKey;
  private String     issuerId;

  /**
   * Constructs an instance of {@link XCRunBuilder}.
   *
   * @param file
   * @param workingDir
   */
  public XCRunBuilder(File file, File workingDir) {
    super(workingDir);
    this.file = file;
  }

  /**
   * Gets the IPA file.
   */
  public final File getFile() {
    return this.file;
  }

  /**
   * Gets the API key.
   */
  public final String getApiKey() {
    return this.apiKey;
  }

  /**
   * Sets the API key.
   */
  public final XCRunBuilder setApiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  /**
   * Gets the issuer identifier.
   */
  public final String getIssuerId() {
    return this.issuerId;
  }

  /**
   * Sets the issuer identifier.
   */
  public final XCRunBuilder setIssuerId(String issuerId) {
    this.issuerId = issuerId;
    return this;
  }

  /**
   * Create the command line for the QMake process.
   */
  @Override
  public final ProcessRequest build() {
    List<String> arguments = new ArrayList<>();
    arguments.add("xcrun");
    arguments.add("altool");
    arguments.add("--upload-app");
    arguments.add("-t");
    arguments.add("ios");
    arguments.add("--apiKey");
    arguments.add(getApiKey());
    arguments.add("--apiIssuer");
    arguments.add(getIssuerId());
    arguments.add("-f");
    arguments.add(getFile().getAbsolutePath());

    return ProcessRequest.create(getWorkingDir(), arguments);
  }
}
