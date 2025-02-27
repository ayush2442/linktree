package com.linktree.linktree.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Async
    @Override
    public void sendWelcomeEmail(String to, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("username", username);

            String htmlContent = templateEngine.process("welcome", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to Linktree!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email", e);
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("token", token);
            context.setVariable("expirationMinutes", jwtExpiration / (60 * 1000));

            String htmlContent = templateEngine.process("password-reset", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Password");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email", e);
        }
    }
}