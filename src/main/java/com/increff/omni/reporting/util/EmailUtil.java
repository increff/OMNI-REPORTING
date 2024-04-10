package com.increff.omni.reporting.util;

import com.increff.omni.reporting.config.EmailProps;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import java.util.Properties;

/**
 * Utility to send emails
 */
public class EmailUtil {

    public static void sendMail(EmailProps eprops) throws MessagingException {
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

        setMessageContent(eprops, message);


        // Send message
        Transport.send(message);

    }

    private static void setMessageContent(EmailProps eprops, Message message) throws MessagingException {

        if (eprops.getIsAttachment()) {
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText("PFA Report");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            String filename = eprops.getAttachment().getAbsolutePath();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(eprops.getCustomizedFileName());
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);
        } else {
            message.setContent(eprops.getContent(), eprops.getContentType());
        }
    }

}
