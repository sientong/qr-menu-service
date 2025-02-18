package com.qrmenu.repository;

import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql("/db/cleanup.sql")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveNewUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.RESTAURANT_OWNER);
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = User.builder()
                .email("find@example.com")
                .passwordHash("hashedPassword")
                .role(UserRole.RESTAURANT_OWNER)
                .build();
        userRepository.save(user);

        // When
        var foundUser = userRepository.findByEmail("find@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("find@example.com");
    }

    @Test
    void shouldUpdateExistingUser() {
        // Given
        User user = User.builder()
                .email("update@example.com")
                .passwordHash("oldPassword")
                .role(UserRole.WAITER)
                .build();
        User savedUser = userRepository.save(user);

        // When
        savedUser.setPasswordHash("newPassword");
        savedUser.setRole(UserRole.ADMIN);
        savedUser.setUpdatedAt(ZonedDateTime.now());
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getPasswordHash()).isEqualTo("newPassword");
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.ADMIN);
    }
} 