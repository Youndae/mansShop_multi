package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ACCESS_DENIED(403, "AccessDeniedException"),
    BAD_CREDENTIALS(403, "BadCredentialsException"),
    TOKEN_STEALING(800, "TokenStealingException"),
    TOKEN_EXPIRED(401, "TokenExpiredException"),
    NOT_FOUND(400, "NotFoundException"),
    NULL_POINTER(500, "NullPointerException"),
    ORDER_SESSION_EXPIRED(440, "OrderSessionExpiredException"),
    ORDER_DATA_FAILED(441, "OrderDataFailedException"),
    DB_CONNECTION_ERROR(500, "DBConnectionError");

    private final int httpStatus;

    private final String message;
}
