package com.qrmenu.exception;

import com.qrmenu.dto.error.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest webRequest = mock(WebRequest.class);

    @Test
    void shouldHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Restaurant", "id", 1);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/restaurants/1");

        // When
        ResponseEntity<ApiError> response = handler.handleResourceNotFoundException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Restaurant not found with id : '1'");
    }

    @Test
    void shouldHandleDuplicateResourceException() {
        // Given
        DuplicateResourceException ex = new DuplicateResourceException("Restaurant", "name", "Test Restaurant");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/restaurants");

        // When
        ResponseEntity<ApiError> response = handler.handleDuplicateResourceException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Restaurant already exists with name : 'Test Restaurant'");
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        List<FieldError> fieldErrors = List.of(
            new FieldError("restaurant", "name", "Name is required"),
            new FieldError("restaurant", "website", "Invalid URL format")
        );
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/restaurants");

        // When
        ResponseEntity<ApiError> response = handler.handleValidationException(ex, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).hasSize(2);
        assertThat(response.getBody().getValidationErrors().get(0).getField()).isEqualTo("name");
        assertThat(response.getBody().getValidationErrors().get(1).getField()).isEqualTo("website");
    }
} 