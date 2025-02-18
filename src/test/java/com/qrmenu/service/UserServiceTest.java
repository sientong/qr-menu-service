package com.qrmenu.service;

import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import com.qrmenu.repository.UserRepository;
import com.qrmenu.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void shouldCreateUserWithHashedPassword() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("password")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenReturn(user);

        // When
        User createdUser = userService.createUser(user);

        // Then
        verify(passwordEncoder).encode("password");
        assertThat(createdUser.getPasswordHash()).isEqualTo("hashedPassword");
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .passwordHash("hashedPassword")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<User> foundUser = userService.findByEmail(email);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }
} 