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

package it.smartio.task.property;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import it.smartio.common.env.Environment;
import it.smartio.common.task.Task;
import it.smartio.common.task.TaskContext;
import it.smartio.util.file.FileMatcher;
import it.smartio.util.file.FilePattern;

/**
 * The {@link PropertyTask} implements a task to replace properties of files by the environment
 * variables.
 */
public class PropertyTask implements Task {

  private final List<Replacer> replacers;

  /**
   * Constructs an instance of {@link PropertyTask}.
   */
  public PropertyTask() {
    this(ReplacerBuilder.DEFAULTS);
  }

  /**
   * Constructs an instance of {@link PropertyTask}.
   *
   * @param replacers
   */
  public PropertyTask(Replacer... replacers) {
    this.replacers = Arrays.asList(replacers);
  }

  /**
   * Constructs an instance of {@link PropertyTask}.
   *
   * @param pattern
   * @param variables
   */
  public PropertyTask(String pattern, List<String> variables) {
    this.replacers = Arrays.asList(new VariableReplacer(pattern, variables));
  }

  /**
   * Applies the different replaces to all matching files.
   *
   * @param context
   */
  @Override
  public final void handle(TaskContext context) throws IOException {
    for (Replacer replacer : this.replacers) {
      for (FileMatcher matcher : FilePattern.matches(context.getWorkingDir(), replacer.getFilePattern())) {
        File file = matcher.getFile();
        if (file.isDirectory()) {
          continue;
        }

        ChangeSet properties = new ChangeSet();
        String output = replacer.replace(file, context.getEnvironment(), properties);
        if (properties.isEmpty()) {
          continue;
        }

        try (PrintWriter writer = new PrintWriter(file)) {
          writer.write(output);
        }

        context.getLogger().onInfo("\nReplaced Properties in '{}'",
            context.getWorkingDir().toPath().relativize(Paths.get(file.getPath())));
        properties.forEach(p -> context.getLogger().onInfo("  {}\t= {} ({})", p.PROPERTY, p.NEW_VALUE, p.OLD_VALUE));
      }
    }
  }

  /**
   * The {@link VariableReplacer} replaces the 'variables' of a line oriented text file.
   */
  class VariableReplacer extends Replacer {

    private final List<String> variables;

    public VariableReplacer(String filepattern, List<String> variables) {
      super(filepattern, PropertyTask.toPattern(variables));
      this.variables = variables;
    }

    @Override
    public final String getValue(String name, String value, Environment environment) {
      return (this.variables.contains(name) && environment.isSet(name)) ? environment.get(name) : value;
    }
  }

  /**
   * Creates a {@link Pattern} string for the defined Variables.
   *
   * @param variables
   */
  private static String toPattern(List<String> variables) {
    return String.format("((%s)[^=]*=)([^\n']+)(\n)", String.join("|", variables));
  }
}
