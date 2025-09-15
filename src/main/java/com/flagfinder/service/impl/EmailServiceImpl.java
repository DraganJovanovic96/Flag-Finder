package com.flagfinder.service.impl;

import com.flagfinder.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService interface.
 * Provides email sending functionality using Spring's JavaMailSender.
 * Supports HTML email content and MIME message handling.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    /**
     * Sends a verification email to the specified recipient.
     * Creates and sends an HTML-enabled MIME message using the configured mail sender.
     *
     * @param to the recipient email address
     * @param subject the email subject line
     * @param text the email body content (supports HTML)
     * @throws MessagingException if email creation or sending fails
     */
    @Override
    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException{
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        emailSender.send(message);
    }
}
