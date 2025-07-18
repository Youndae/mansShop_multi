package com.example.moduleapi.config.exception;

import lombok.Builder;

public record ExceptionEntity(
        String errorCode,
        String errorMessage
) {
}
