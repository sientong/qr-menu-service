package com.qrmenu.service;

import com.qrmenu.dto.auth.TokenResponse;
import com.qrmenu.model.User;

public interface AuthenticationService {
    TokenResponse login(String email, String password);
    TokenResponse refreshToken(String refreshToken);
    void logout(String accessToken);
    void logoutAll(Long userId);
    User getCurrentUser(String accessToken);
    boolean validateToken(String token);
    void requestPasswordReset(String email);
    void confirmPasswordReset(String token, String newPassword);
} 