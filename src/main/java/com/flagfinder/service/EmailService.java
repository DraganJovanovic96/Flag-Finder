package com.flagfinder.service;

import jakarta.mail.MessagingException;

/**
 * EmailService interface for managing emails.
 * The EmailService interface contains methods that will be implemented is EmailServiceImpl.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
public interface EmailService {

    /**
     * Sends a verification email to the specified recipient.
     *
     * @param to the recipient email address
     * @param Subject the email subject line
     * @param text the email body content
     * @throws MessagingException if email sending fails
     */
    public void sendVerificationEmail(String to, String Subject, String text) throws MessagingException;
}
