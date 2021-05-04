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

package it.smartio.util.xml;

import java.io.InputStream;
import java.io.Writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerFactory;

/**
 * The {@link StAX} class
 */
public abstract class StAX {

  private static final XMLInputFactory    ReaderFactory = XMLInputFactory.newInstance();
  private static final XMLOutputFactory   WriterFactory = XMLOutputFactory.newInstance();
  private static final TransformerFactory Transformer   = TransformerFactory.newInstance();

  static {
    StAX.ReaderFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
    StAX.WriterFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
    if (StAX.WriterFactory.isPropertySupported("com.ctc.wstx.useDoubleQuotesInXmlDecl")) {
      StAX.WriterFactory.setProperty("com.ctc.wstx.useDoubleQuotesInXmlDecl", Boolean.TRUE);
    }
    StAX.Transformer.setAttribute("indent-number", 2);
  }

  public static final String ENCODING = "UTF-8";
  public static final String VERSION  = "1.0";

  /**
   * Constructs a(n) {@link StAX} object.
   */
  private StAX() {}

  /**
   * Creates an instance of {@link XMLStreamReader}.
   *
   * @param stream
   */
  public static XMLStreamReader createReader(InputStream stream) throws XMLStreamException {
    return StAX.ReaderFactory.createXMLStreamReader(stream, "UTF-8");
  }

  /**
   * Creates an instance of {@link XMLStreamWriter}.
   *
   * @param writer
   */
  public static XMLStreamWriter createWriter(Writer writer) throws XMLStreamException {
    return StAX.WriterFactory.createXMLStreamWriter(writer);
  }
}
