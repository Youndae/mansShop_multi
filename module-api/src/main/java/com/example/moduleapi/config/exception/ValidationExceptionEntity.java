package com.example.moduleapi.config.exception;

import java.util.List;

public record ValidationExceptionEntity(
        int errorCode,
        String errorMessage,
        List<ValidationError> errors
) {
}
