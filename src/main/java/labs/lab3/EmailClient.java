package labs.lab3;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.Properties;

public class EmailClient {
    public static void main(String[] args) {
        // SMTP-server settings
        String smtpHost = "smtp.mail.ru";
        String smtpPort = "465";

        // User settings
        String emailUsername = "";
        String emailPassword = "";

        // Letter settings
        String recipientAddress = "";
        String subject = "Daily image";
        String bodyText = "Hello! I hope this picture will make your day better!";

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        };

        Session session = Session.getInstance(getProperties(smtpHost, smtpPort), authenticator);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAddress));
            message.setSubject(subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(bodyText);

            String imagePath = "src\\main\\java\\labs\\lab3\\mem.jpg";

            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.attachFile(imagePath);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Mail successfully sent.");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private static Properties getProperties(String smtpHost, String smtpPort) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.socketFactory.port", smtpPort);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //Установка SSL Соединения
        properties.put("mail.smtp.socketFactory.fallback", "false");
        return properties;
    }
}
