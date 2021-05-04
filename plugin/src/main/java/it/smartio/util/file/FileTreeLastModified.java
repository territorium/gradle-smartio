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

package it.smartio.util.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * The {@link FileTreeLastModified} copies a directory structure from source to the target path.
 */
public final class FileTreeLastModified extends SimpleFileVisitor<Path> {

  private Instant instant;


  /**
   * Resolves the path.
   *
   * @param path
   */
  private Instant toInstant(Path path) {
    try {
      BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
      return attr.creationTime().toInstant();
    } catch (Throwable ex) {}
    return null;
  }

  /**
   * Visit a directory.
   *
   * @param path
   * @param attrs
   */
  @Override
  public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  /**
   * Visit a file.
   *
   * @param path
   * @param attrs
   */
  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
    try {
      BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
      Instant current = attr.creationTime().toInstant();
      if ((this.instant == null) || current.isAfter(this.instant)) {
        this.instant = current;
      }
    } catch (Throwable ex) {
      throw new IOException(ex);
    }
    return FileVisitResult.CONTINUE;
  }


  /**
   * Copy the file tree using the environment variables.
   *
   * @param source
   */
  public static LocalDate lastModified(Path source) throws IOException {
    FileTreeLastModified visitor = new FileTreeLastModified();
    Files.walkFileTree(source, visitor);
    return visitor.instant == null ? LocalDate.now()
        : LocalDateTime.ofInstant(visitor.instant, ZoneOffset.UTC).toLocalDate();
  }
}