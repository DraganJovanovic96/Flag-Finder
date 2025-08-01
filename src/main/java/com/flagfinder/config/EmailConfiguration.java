package com.flagfinder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * EmailConfiguration is a configuration class that defines the beans and dependencies required
 * sending emails to users.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class EmailConfiguration {
    /**
     * The email username for authentication with the mail server.
     */
    @Value("${spring.mail.username}")
    private String emailUsername;

    /**
     * The email password for authentication with the mail server.
     */
    @Value("${spring.mail.password}")
    private String password;

    /**
     * Configures and provides a {@link JavaMailSender} bean for sending emails.
     *
     * @return a configured {@link JavaMailSender} instance
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
