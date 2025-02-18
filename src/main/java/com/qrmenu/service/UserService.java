package com.qrmenu.service;

import com.qrmenu.model.User;
import java.util.Optional;

public interface UserService {
    User createUser(User user);

    User updateUser(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    User getCurrentUser();
} 