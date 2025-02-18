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
class UserServiceImplTest {

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
        String rawPassword = "password123";
        String hashedPassword = "hashedPassword123";
        User user = User.builder()
                .email("test@example.com")
                .passwordHash(rawPassword)
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // When
        User createdUser = userService.createUser(user);

        // Then
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getPasswordHash()).isEqualTo(hashedPassword);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<User> foundUser = userService.findByEmail(email);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }

    @Test
    void shouldCheckIfUserExistsByEmail() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // When
        boolean exists = userService.existsByEmail(email);

        // Then
        assertThat(exists).isTrue();
        verify(userRepository).findByEmail(email);
    }
} 