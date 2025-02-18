package com.qrmenu.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UrlValidatorTest {

    private UrlValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new UrlValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.com",
        "http://localhost:8080",
        "https://sub.domain.com/path?param=value",
        "http://192.168.1.1:8080"
    })
    void shouldValidateValidUrls(String url) {
        assertThat(validator.isValid(url, context)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "not-a-url",
        "ftp://invalid-protocol.com",
        "http://",
        "https://.com",
        "http://invalid space.com"
    })
    void shouldNotValidateInvalidUrls(String url) {
        assertThat(validator.isValid(url, context)).isFalse();
    }

    @Test
    void shouldAllowNullValues() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    void shouldAllowEmptyValues() {
        assertThat(validator.isValid("", context)).isTrue();
    }
} 