/*
 * Copyright (c) 2001-2022 Territorium Online Srl / TOL GmbH. All Rights Reserved.
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

package it.smartio.util.svg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link SVGNode} class.
 */
class SVGNode {

  private final String name;
  private String       content;


  private final Map<String, String> attrs    = new HashMap<>();
  private final List<SVGNode>       children = new ArrayList<>();

  /**
   * Constructs an instance of {@link SVGNode}.
   *
   * @param name
   * @param content
   */
  public SVGNode(String name, String content) {
    this.name = name;
    this.content = content;
  }

  /**
   * Constructs an instance of {@link SVGNode}.
   *
   * @param content
   */
  public SVGNode(String content) {
    this("", content);
  }

  /**
   * Constructs an instance of {@link SVGNode}.
   */
  public SVGNode() {
    this(null, null);
  }

  public final String getName() {
    return this.name;
  }

  public Set<String> getAttributes() {
    return this.attrs.keySet();
  }

  public List<SVGNode> getChildren() {
    return this.children;
  }

  public final String getAttribute(String name) {
    return this.attrs.get(name);
  }

  public final String getValue() {
    return this.content.isEmpty() && !this.children.isEmpty()
        ? this.children.stream().map(SVGNode::toString).collect(Collectors.joining())
        : this.content;
  }

  public final Iterator<SVGNode> iterator() {
    return this.children.iterator();
  }

  /**
   * Returns a string representation of the {@link SVGNode}.
   */
  @Override
  public String toString() {
    return this.name == null ? this.content
        : String.format(
            "<%s%s>%s</%s>", this.name, this.attrs.keySet().stream()
                .map(k -> String.format(" %s=\"%s\"", k, this.attrs.get(k))).collect(Collectors.joining()),
            getValue(), this.name);
  }

  /**
   * Set an attribute.
   *
   * @param name
   * @param value
   */
  public void setAttribute(String name, String value) {
    this.attrs.put(name, value);
  }

  /**
   * Set an attribute.
   *
   * @param name
   * @param value
   */
  public void setAttribute(String name, String value, String namespace) {
    this.attrs.put(namespace + ":" + name, value);
  }

  /**
   * Sets the {@link SVGNode} content.
   *
   * @param content
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Adds a child {@link SVGNode}.
   *
   * @param node
   */
  public void addNode(SVGNode node) {
    this.children.add(node);
  }

  /**
   * Creates a text node.
   *
   * @param characters
   */
  void handleCharacters(List<String> characters) {
    String text = characters.stream().collect(Collectors.joining());
    if (!text.trim().isEmpty()) {
      setContent(text);
    }
    characters.clear();
  }
}
