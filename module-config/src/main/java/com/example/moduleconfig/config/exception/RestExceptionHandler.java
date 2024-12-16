package com.example.moduleconfig.config.exception;

import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleconfig.config.exception.customException.*;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
@Hidden
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomTokenExpiredException.class)
    public ResponseEntity<?> tokenExpiredException(Exception e) {
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

    @ExceptionHandler({CustomBadCredentialsException.class, BadCredentialsException.class})
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

    private ResponseEntity<?> toResponseEntity(ErrorCode errorCode){

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        ExceptionEntity.builder()
                                .errorCode(String.valueOf(errorCode.getHttpStatus()))
                                .errorMessage(errorCode.getMessage())
                                .build()
                );
    }

}
