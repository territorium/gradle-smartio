/**
 * 
 */

package it.smartio.gocd.task.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.gocd.util.OS;

/**
 * 
 */
public class GradleBuilder {

  private static final Pattern ARGUMENTS = Pattern.compile("(?:-P)?([^=\\s\n]+)(?:([=\\s])([^\n]+))?");


  private final String       command;
  private final List<String> arguments;


  private GradleBuilder(String command) {
    this.command = command;
    this.arguments = new ArrayList<>();
  }

  /**
   * Formats a Gradle parameter for the specific platform.
   *
   * @param name
   * @param value
   */
  private static String getParameter(String name, String value) {
    String pattern = OS.isWindows() ? "-P%s=%s" : "-P%s='%s'";
    return String.format(pattern, name, value);
  }

  /**
   * Parses the arguments from input.
   * 
   * @param parameter
   */
  public final void parseArguments(String parameter) {
    if (parameter != null) {
      Matcher matcher = GradleBuilder.ARGUMENTS.matcher(parameter.replace("\\\n", ""));
      while (matcher.find()) {
        if ("=".equals(matcher.group(2))) {
          arguments.add(GradleBuilder.getParameter(matcher.group(1).trim(), matcher.group(3).trim()));
        } else {
          arguments.add(matcher.group(1));
          if (matcher.group(3) != null) {
            arguments.add(matcher.group(3));
          }
        }
      }
    }
  }

  /**
   * Build the gradle arguments list for a shell
   */
  public final List<String> build() {
    List<String> args = new ArrayList<>();
    args.add(OS.isWindows() ? "gradlew.bat" : "./gradlew");
    args.add(command);
    arguments.forEach(a -> args.add(a));

    List<String> commands = new ArrayList<>();
    commands.add(OS.isWindows() ? "cmd" : "sh");
    commands.add(OS.isWindows() ? "/c" : "-c");
    commands.add(String.join(" ", args));
    return commands;
  }

  /**
   * Creates a new instance of {@link GradleBuilder}
   * 
   * @param command
   */
  public static GradleBuilder create(String command) {
    return new GradleBuilder(command);
  }
}
