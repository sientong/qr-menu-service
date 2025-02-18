package com.qrmenu.service;

import com.qrmenu.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void shouldSendPasswordResetEmail() {
        // Given
        String email = "test@example.com";
        String token = "reset-token";
        String frontendUrl = "http://localhost:3000";
        String fromEmail = "noreply@test.com";

        ReflectionTestUtils.setField(emailService, "frontendUrl", frontendUrl);
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);

        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset"), any(Context.class)))
                .thenReturn("<html>Reset password</html>");

        // When
        emailService.sendPasswordResetEmail(email, token);

        // Then
        verify(emailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq("password-reset"), any(Context.class));
    }
} 