package com.zhiar.utils;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtils.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${mailjet.from.email}")
    private String fromEmail;

    @PostConstruct
    public void init() {
        LOGGER.info("EmailUtils initialized with JavaMailSender.");
    }

    public void sendSimpleMessage(String to, String subject, String text, List<String> ccList) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            if (ccList != null && !ccList.isEmpty()) {
                message.setCc(ccList.toArray(new String[0]));
            }

            emailSender.send(message);

            LOGGER.info("Email sent to {} with subject {}", to, subject);

        } catch (Exception e) {
            LOGGER.error("Exception occurred while sending email: ", e);
        }
    }

    public ResponseEntity<String> forgotMail(String email, String subject, String password) {
        try {
            // Create a MimeMessage
            MimeMessage mimeMessage = emailSender.createMimeMessage();

            // Create a MimeMessageHelper to set content
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(email);
            messageHelper.setSubject(subject);

            // Set the HTML content
            String htmlContent = "<p><b>Your Login detail for Cafe Management System</b><br><b>Email : </b>" + email +
                    "<br><b>Password: </b>" + password +
                    "<br><a href=\"http://localhost:8080/\">Click here to login</a></p>";
            messageHelper.setText(htmlContent, true); // true indicates that the text is HTML

            // Send the email
            emailSender.send(mimeMessage);

            return ResponseEntity.ok("Email sent successfully.");

        } catch (Exception e) {
            LOGGER.error("Exception occurred while sending email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email. Please try again later.");
        }
    }
}