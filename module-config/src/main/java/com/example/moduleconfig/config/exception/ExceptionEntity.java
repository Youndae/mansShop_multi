package com.example.moduleconfig.config.exception;

import lombok.Builder;

public record ExceptionEntity(
        String errorCode,
        String errorMessage
) {

    @Builder
    public ExceptionEntity{}
}
