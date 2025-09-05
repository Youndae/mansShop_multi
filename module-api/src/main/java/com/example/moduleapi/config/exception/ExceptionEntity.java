package com.example.moduleapi.config.exception;

public record ExceptionEntity(
        int errorCode,
        String errorMessage
) {
}
