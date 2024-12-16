package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ACCESS_DENIED(403, "AccessDeniedException")
    , BAD_CREDENTIALS(403, "BadCredentialsException")
    , TOKEN_STEALING(800, "TokenStealingException")
    , TOKEN_EXPIRED(401, "TokenExpiredException")
    , NOT_FOUND(400, "NotFoundException")
    , NULL_POINTER(500, "NullPointerException");

    private final int httpStatus;

    private final String message;
}
