package com.qrmenu.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetToken);
    void sendPasswordChangedEmail(String to);
} 