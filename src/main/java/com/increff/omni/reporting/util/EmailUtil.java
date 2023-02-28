package com.increff.omni.reporting.util;

import com.increff.omni.reporting.config.EmailProps;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Utility to send emails
 */
public class EmailUtil {

    public static void sendMail(EmailProps eprops) throws MessagingException, javax.mail.MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", eprops.getSmtpHost());
        props.put("mail.smtp.port", eprops.getSmtpPort());

        // Get the Session object.
        final PasswordAuthentication authn = new PasswordAuthentication(eprops.getUsername(),
                eprops.getPassword());
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return authn;
            }
        };
        Session session = Session.getInstance(props, authenticator);

        // Create a default MimeMessage object.
        Message message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(eprops.getFromEmail()));

        // Set To: header field of the header.
        for (String email : eprops.getToEmails()) {
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        }
        // Set Subject: header field
        message.setSubject(eprops.getSubject());

        // Send the actual HTML message, as big as you like
        message.setContent(eprops.getContent(), eprops.getContentType());

        // Send message
        Transport.send(message);

    }

    /**
     * Fetch and encode template to String
     *
     * @param resourcePath Path to template
     * @return Encoded base template String
     */
    public static String getTemplate(String resourcePath) throws IOException {
        InputStream is = EmailUtil.class.getResourceAsStream(resourcePath);
        String query = IOUtils.toString(is, "utf-8");
        closeQuietly(is);
        return query;
    }

    private static void closeQuietly(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Throwable t) {
            //Do nothing
        }
    }

}
