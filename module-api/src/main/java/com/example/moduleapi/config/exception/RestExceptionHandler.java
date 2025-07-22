package com.example.moduleapi.config.exception;

import com.example.modulecommon.customException.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomTokenExpiredException.class)
    public ResponseEntity<ExceptionEntity> tokenExpiredException(Exception e) {
        log.warn("tokenExpiredException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.TOKEN_EXPIRED);
    }

    @ExceptionHandler({CustomAccessDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<?> accessDeniedException(Exception e) {
        log.warn("AccessDeniedException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(CustomTokenStealingException.class)
    public ResponseEntity<?> tokenStealingException(Exception e) {
        log.warn("TokenStealing : {}", e.getMessage());

        return toResponseEntity(ErrorCode.TOKEN_STEALING);
    }

    @ExceptionHandler({CustomBadCredentialsException.class, BadCredentialsException.class, InternalAuthenticationServiceException.class})
    public ResponseEntity<?> badCredentialsException(Exception e) {
        log.warn("BadCredentials Exception : {}", e.getMessage());

        return toResponseEntity(ErrorCode.BAD_CREDENTIALS);
    }

    @ExceptionHandler({CustomNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<?> notFoundException(Exception e) {
        log.warn("NotFoundException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> nullPointerException(Exception e) {
        log.warn("nullPointerException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(CustomOrderSessionExpiredException.class)
    public ResponseEntity<?> orderSessionExpiredException(Exception e) {
        log.warn("orderSessionExpiredException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.ORDER_SESSION_EXPIRED);
    }

    @ExceptionHandler(CustomOrderDataFailedException.class)
    public ResponseEntity<?> orderDataFailedException(Exception e) {
        log.warn("orderDataFailedException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.ORDER_DATA_FAILED);
    }

    @ExceptionHandler({CannotCreateTransactionException.class, JpaSystemException.class, SQLException.class})
    public ResponseEntity<?> cannotCreateTransactionException(Exception e) {
        log.warn("DB Connection Exception : {}", e.getMessage());

        return toResponseEntity(ErrorCode.DB_CONNECTION_ERROR);
    }

    private ResponseEntity<ExceptionEntity> toResponseEntity(ErrorCode errorCode) {

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        new ExceptionEntity(
                                String.valueOf(errorCode.getHttpStatus()),
                                errorCode.getMessage()
                        )
                );
    }
}
