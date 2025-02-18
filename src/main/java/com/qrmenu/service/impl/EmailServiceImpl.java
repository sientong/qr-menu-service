package com.qrmenu.service.impl;

import com.qrmenu.service.EmailService;
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

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("resetUrl", 
                    frontendUrl + "/reset-password?token=" + resetToken);

            String emailContent = templateEngine.process("password-reset", context);
            sendEmail(to, "Password Reset Request", emailContent);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", to, e);
        }
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String to) {
        try {
            Context context = new Context();
            String emailContent = templateEngine.process("password-changed", context);
            sendEmail(to, "Password Changed Successfully", emailContent);
        } catch (Exception e) {
            log.error("Failed to send password changed email to {}", to, e);
        }
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        
        emailSender.send(message);
    }
} 