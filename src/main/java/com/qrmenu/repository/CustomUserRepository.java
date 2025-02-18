package com.qrmenu.repository;

import com.qrmenu.model.User;
import java.util.Optional;

public interface CustomUserRepository {
    User saveUser(User user);
    Optional<User> findByEmail(String email);
    void delete(Long id);
} 