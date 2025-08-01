package com.mss.service;

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

    public void sendVerificationEmail(String to, String Subject, String text) throws MessagingException;
}
