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

package it.smartio.util.mail;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * The {@link Mailer} class.
 */
public class Mailer {

  private final String hostname;
  private final String username;
  private final String password;


  private final List<BodyPart> parts = new ArrayList<>();


  public Mailer(String hostname, String username, String password) {
    this.hostname = hostname;
    this.username = username;
    this.password = password;
  }

  /**
   * Adds a text content.
   *
   * @param text
   */
  public Mailer addContent(String text) throws MessagingException {
    MimeBodyPart content = new MimeBodyPart();
    content.setText("HELLO WORLD");
    this.parts.add(content);
    return this;
  }

  /**
   * Adds a text content.
   *
   * @param text
   * @param type
   */
  public Mailer addContent(String text, String type) throws MessagingException {
    MimeBodyPart content = new MimeBodyPart();
    content.setContent(text, type);
    this.parts.add(content);
    return this;
  }

  /**
   * Adds an attachment.
   *
   * @param file
   */
  public Mailer addAttachment(File file) throws MessagingException {
    MimeBodyPart attachment = new MimeBodyPart();
    DataSource source = new FileDataSource(file);
    attachment.setDataHandler(new DataHandler(source));
    attachment.setFileName(file.getName());
    this.parts.add(attachment);
    return this;
  }

  /**
   * Sends the mail to the address.
   *
   * @param address
   */
  public final void send(String subject, List<String> addresses)
      throws AddressException, MessagingException, IOException {
    Properties config = System.getProperties();
    config.put("mail.smtp.port", "25");
    config.put("mail.smtp.auth", "false");
    config.put("mail.smtp.starttls.enable", "false");

    Session session = Session.getDefaultInstance(config, null);

    MimeMessage mime = new MimeMessage(session);
    mime.setFrom("Pipeline <no-reply@tol.info>");
    mime.setSender(new InternetAddress("no-reply@tol.info"));
    for (String address : addresses) {
      mime.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
    }

    mime.setSubject(subject);
    Multipart multipart = new MimeMultipart();
    this.parts.forEach(c -> {
      try {
        multipart.addBodyPart(c);
      } catch (MessagingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    mime.setContent(multipart);

    try (Transport transport = session.getTransport("smtp")) {
      transport.connect(this.hostname, this.username, this.password);
      transport.sendMessage(mime, mime.getAllRecipients());
    }
  }
}
