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

package it.smartio.util.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import it.smartio.util.xml.StAX;

/**
 * The {@link SVGDocument} class.
 */
public class SVGDocument extends SVGNode {

  private static final String NAME      = "svg";
  private static final String NAMESPACE = "http://www.w3.org/2000/svg";
  private static final String XML       = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

  /**
   * Constructs an instance of {@link SVGDocument}.
   */
  public SVGDocument() {
    super(SVGDocument.NAME, "");
    setAttribute("xmlns", SVGDocument.NAMESPACE);
    setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
  }

  /**
   * Returns a string representation of the {@link SVGNode}.
   */
  @Override
  public final String toString() {
    return SVGDocument.XML + super.toString();
  }

  /**
   * Parses an {@link SVGDocument} from the {@link XMLStreamReader}.
   *
   * @param reader
   */
  public static SVGDocument parse(XMLStreamReader reader) throws XMLStreamException {
    SVGDocument document = new SVGDocument();
    Stack<SVGNode> nodes = new Stack<>();
    List<String> characters = new ArrayList<>();

    while (reader.hasNext()) {
      switch (reader.next()) {
        case XMLStreamConstants.START_ELEMENT:
          if (!nodes.isEmpty()) {
            nodes.peek().handleCharacters(characters);
          }

          String name = reader.getLocalName();
          String namespace = reader.getNamespaceURI();
          SVGNode node = name.equals(SVGDocument.NAME) ? document : new SVGNode(name, "");
          if ((node != document) && namespace.equals(SVGDocument.NAMESPACE)) {
            nodes.peek().addNode(node);
          }
          nodes.push(node);

          for (int index = 0; index < reader.getAttributeCount(); index++) {
            String attrNs = reader.getAttributePrefix(index);
            String attrName = reader.getAttributeLocalName(index);
            String attrValue = reader.getAttributeValue(index);
            if (attrNs.isEmpty()) {
              node.setAttribute(attrName, attrValue);
            } else if ("xlink".equalsIgnoreCase(attrNs)) {
              node.setAttribute(attrName, attrValue, attrNs);
              document.setAttribute("xmlns:" + attrNs, reader.getAttributeNamespace(index));
            }
          }
          break;

        case XMLStreamConstants.END_ELEMENT:
          node = nodes.pop();
          node.handleCharacters(characters);
          break;

        case XMLStreamConstants.CDATA:
        case XMLStreamConstants.CHARACTERS:
          characters.add(reader.getText());
          break;

        case XMLStreamConstants.COMMENT:
          node = new SVGNode(reader.getText());
          break;
      }
    }

    return document;
  }

  /**
   * Parses an {@link SVGDocument} from the {@link InputStream}.
   *
   * @param file
   */
  public static SVGDocument parse(File file) throws IOException {
    try (InputStream stream = new FileInputStream(file)) {
      return SVGDocument.parse(StAX.createReader(stream));
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }
}
