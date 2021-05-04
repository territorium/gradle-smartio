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

package it.smartio.common.task;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import it.smartio.common.env.Environment;

/**
 * The {@link TaskContextAsync} class.
 */
public abstract class TaskContextAsync implements TaskContext, Closeable {

  private final File            workingDir;
  private final Environment     environment;
  private final ExecutorService executor = Executors.newFixedThreadPool(2);

  /**
   * Constructs an instance of {@link TaskContextAsync}.
   *
   * @param workingDir
   * @param environment
   */
  protected TaskContextAsync(File workingDir, Environment environment) {
    this.workingDir = workingDir;
    this.environment = environment;
  }

  /**
   * Gets the working directory.
   */
  @Override
  public final File getWorkingDir() {
    return this.workingDir;
  }

  /**
   * Gets the environment variable.
   */
  @Override
  public final Environment getEnvironment() {
    return this.environment;
  }

  /**
   * Setup the console to read the input stream as standard error.
   *
   * @param input
   * @param error
   */
  protected final void redirectStreams(InputStream input, InputStream error) {
    this.executor.submit(new AsyncInputStream(input, System.out::println));
    this.executor.submit(new AsyncInputStream(error, System.err::println));
  }

  @Override
  public final void close() throws IOException {
    this.executor.shutdownNow();
  }

  /**
   * The {@link AsyncInputStream} class.
   */
  private class AsyncInputStream implements Runnable {

    private final InputStream      stream;
    private final Consumer<String> consumer;

    public AsyncInputStream(InputStream stream, Consumer<String> consumer) {
      this.stream = stream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(this.stream)).lines().forEach(this.consumer);
    }
  }
}
