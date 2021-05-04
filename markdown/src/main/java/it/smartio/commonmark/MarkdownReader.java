/*
 * Copyright (c) 2001-2024 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.commonmark;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.smartio.docs.util.Replacer;

/**
 * The {@link MarkdownReader} implements a reader based on MARKDOWN. The Reader starts from a file
 * an includes referenced files to create a single huge MARKDOWN file.
 */
public class MarkdownReader {

  // File includes: [TITLE?](RELATIVE_PATH)
  private static final Pattern INCLUDE =
      Pattern.compile("^(?:([#]+)\\s*)?\\[([^\\]]*)\\]\\((.+\\.md)\\)", Pattern.CASE_INSENSITIVE);

  // Header definition: #+ HEADER
  private static final Pattern HEADER = Pattern.compile("^([#]+)\\s*(.+)", Pattern.CASE_INSENSITIVE);

  // Footnote definition: [ID]: URL "TITLE"
  private static final Pattern FOOTNOTE =
      Pattern.compile("^\\[(\\w+)\\]:\\s([^\\s]+)(\\s\".+\")?", Pattern.CASE_INSENSITIVE);

  // Footnote reference: [TEXT]?[ID]
  private static final Pattern FOOTNOTE_REF =
      Pattern.compile("(?:\\[([^\\]]*)\\])\\[(\\w+)\\]", Pattern.CASE_INSENSITIVE);

  // Image definition: ![](IMAGE_PATH)
  private static final Pattern IMAGE = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);

  private final File           file;

  /**
   * Constructs an instance of {@link MarkdownReader}.
   *
   * @param file
   */
  public MarkdownReader(File file) {
    this.file = file;
  }

  /**
   * Gets the file.
   */
  protected final File getFile() {
    return file;
  }

  /**
   * Merge the files to the markdown content.
   *
   * @param file
   * @param header
   * @param request
   */
  protected final void merge(File file, String header, String title, Request request) throws IOException {
    boolean isCode = false;
    String level = header;
    File path = file.getParentFile().getAbsoluteFile();
    for (String line : Files.readAllLines(file.toPath())) {
      if (line.startsWith("~~~")) {
        request.writeln(line);
        isCode = !isCode;
        continue;
      }

      if (isCode) {
        request.writeln(line);
        continue;
      }

      // Ignore lines that starting with a tab
      if (line.startsWith("\t")) {
        request.writeln(line);
        continue;
      }

      // Processes file includes.
      Matcher matcher = MarkdownReader.INCLUDE.matcher(line);
      if (matcher.find()) {
        File md = new File(path, matcher.group(3));
        if (md.exists()) {
          String hash = matcher.group(1) == null ? level : matcher.group(1).substring(1);
          String subtitle = matcher.group(2) == null ? title : matcher.group(2);
          merge(md, "\n" + hash, subtitle, request);
        }
        continue;
      }

      // Update headers
      matcher = MarkdownReader.HEADER.matcher(line);
      if (matcher.find()) {
        level = header + matcher.group(1);
        if (matcher.group(1).equals("#") && title != null) {
          request.writeln(level + " " + title);
        } else {
          request.writeln(level + " " + matcher.group(2));
        }
        continue;
      }

      // Update footnotes
      matcher = MarkdownReader.FOOTNOTE.matcher(line);
      if (matcher.find()) {
        request.addFootNote(file.getName(), matcher.group(1), matcher.group(2), matcher.group(3));
        continue;
      }

      // Replace all link references
      line = Replacer.replaceAll(line, MarkdownReader.FOOTNOTE_REF,
          m -> request.getFootNote(file.getName(), m.group(2), m.group(1)));

      // Normalize image paths
      line = Replacer.replaceAll(line, MarkdownReader.IMAGE,
          m -> m.group(2).startsWith("/") || m.group(2).startsWith("data:") ? m.group(0)
              : String.format("![%s](%s/%s)", m.group(1), path.getPath(), m.group(2)));

      request.writeln(line);
    }
  }

  /**
   * Defines a rendering request.
   */
  private class Request implements Closeable {

    private final PrintWriter          writer;
    private final List<String>         indexes;
    private final Map<Integer, String> footnotes;

    /**
     * Creates an instance of {@link Request}.
     * 
     * @param writer
     */
    public Request(PrintWriter writer) {
      this.writer = writer;
      this.indexes = new ArrayList<>();
      this.footnotes = new HashMap<>();
    }

    /**
     * Creates an instance of {@link Request}.
     * 
     * @param writer
     */
    public void addFootNote(String file, String name, String uri, String title) {
      int index = this.indexes.indexOf(String.format("%s_%s", file, name)) + 1;
      String text = String.format("[%s]: %s%s", index, uri, (title == null) ? "" : title);
      this.footnotes.put(index, text);
    }

    /**
     * Calculates the new footnote reference.
     *
     * @param file
     * @param name
     * @param text
     */
    public final String getFootNote(String file, String name, String text) {
      this.indexes.add(String.format("%s_%s", file, name));
      int index = this.indexes.size();
      return (text == null) ? String.format("[%s]", index) : String.format("[%s][%s]", text, index);
    }

    /**
     * Print the text to the writer.
     * 
     * @param text
     */
    public final void writeln(String text) {
      this.writer.println(text);
    }

    /**
     * Closes this request and releases resources associated with it.
     */
    @Override
    public final void close() throws IOException {
      if (!footnotes.isEmpty()) {
        writer.println();
        footnotes.keySet().stream().sorted().forEach(i -> writer.println(footnotes.get(i)));
        writer.println();
      }
      writer.close();
    }
  }

  /**
   * Reads all data.
   */
  public final String readAll() throws IOException {
    StringWriter text = new StringWriter();
    try (Request request = new Request(new PrintWriter(text))) {
      merge(getFile(), "", null, request);
    }
    return text.toString();
  }
}
