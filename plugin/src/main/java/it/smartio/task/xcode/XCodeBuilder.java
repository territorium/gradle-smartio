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
 * The {@link XCodeBuilder} class.
 */
public class XCodeBuilder extends ProcessRequestBuilder {

  public enum XCodeOperation {
    ANALYZE,
    ARCHIVE,
    EXPORT
  }


  private final String   targetName;

  private XCodeOperation operation;
  private File           exportPList;
  private String         exportType;
  private String         identifier;
  private String         developmentTeam;

  /**
   * Constructs an instance of {@link XCodeBuilder}.
   *
   * @param targetName
   */
  public XCodeBuilder(String targetName, File workingDir) {
    super(workingDir);
    this.targetName = targetName;
    this.operation = XCodeOperation.ANALYZE;
    this.exportType = "Release";
  }

  /**
   * Get the target name
   */
  public final String getTargetName() {
    return this.targetName;
  }

  /**
   * Gets the {@link XCodeOperation}.
   */
  public final XCodeOperation getXCodeOperation() {
    return this.operation;
  }

  /**
   * Sets the {@link XCodeOperation}.
   */
  public final XCodeBuilder setXCodeOperation(XCodeOperation operation) {
    this.operation = operation;
    return this;
  }

  /**
   * Gets the PList.
   */
  public final File getExportPList() {
    return this.exportPList;
  }

  /**
   * Sets the PList.
   */
  public final XCodeBuilder setExportPList(File exportPList) {
    this.exportPList = exportPList;
    return this;
  }

  /**
   * Gets the release type.
   */
  public final String getExportType() {
    return this.exportType;
  }

  /**
   * Sets the release type.
   */
  public final XCodeBuilder setExportType(String exportType) {
    this.exportType = exportType;
    return this;
  }

  /**
   * Gets the release identifier.
   */
  public final String getIdentifier() {
    return this.identifier;
  }

  /**
   * Sets the release identifier.
   */
  public final XCodeBuilder setIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * Gets the development team.
   */
  public final String getDevelopmentTeam() {
    return this.developmentTeam;
  }

  /**
   * Sets the development team.
   */
  public final XCodeBuilder setDevelopmentTeam(String developmentTeam) {
    this.developmentTeam = developmentTeam;
    return this;
  }

  /**
   * Create the command line for the QMake process.
   */
  @Override
  public final ProcessRequest build() {
    List<String> arguments = new ArrayList<>();
    arguments.add("xcodebuild");

    switch (getXCodeOperation()) {
      case ARCHIVE:
        arguments.add("-project");
        arguments.add(getTargetName() + ".xcodeproj");
        arguments.add("-scheme");
        arguments.add(getTargetName());
        arguments.add("-sdk");
        arguments.add("iphoneos");

        arguments.add("-configuration");
        arguments.add(getExportType()); // AppStoreDistribution

        arguments.add("archive");
        arguments.add("-archivePath");
        arguments.add(getTargetName() + ".xcarchive");
        arguments.add("DEVELOPMENT_TEAM=" + getDevelopmentTeam());
        arguments.add("PRODUCT_BUNDLE_IDENTIFIER=" + getIdentifier());
        arguments.add("ENABLE_BITCODE=NO");
        arguments.add("-UseModernBuildSystem=YES");
        break;

      case EXPORT:
        arguments.add("-exportArchive");
        arguments.add("-archivePath");
        arguments.add(getTargetName() + ".xcarchive");
        arguments.add("-exportOptionsPlist");
        arguments.add(getExportPList().getAbsolutePath());
        arguments.add("-allowProvisioningUpdates");
        arguments.add("-exportPath");
        arguments.add(getWorkingDir().getAbsolutePath());
        arguments.add("PRODUCT_BUNDLE_IDENTIFIER=" + getIdentifier());
        arguments.add("-UseModernBuildSystem=NO");
        break;

      default:
        arguments.add("-project");
        arguments.add(getTargetName() + ".xcodeproj");
        arguments.add("-scheme");
        arguments.add(getTargetName());
        arguments.add("-sdk");
        arguments.add("iphoneos");
        arguments.add("clean");
        break;
    }

    return ProcessRequest.create(getWorkingDir(), arguments);
  }
}
