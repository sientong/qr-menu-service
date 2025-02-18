package com.qrmenu.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Let @NotNull or @NotEmpty handle this
        }
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
} 