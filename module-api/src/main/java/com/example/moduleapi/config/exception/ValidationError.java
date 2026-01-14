package com.example.moduleapi.config.exception;

public record ValidationError(
        String field,
        String constraint,
        String validationMessage
) {
}
