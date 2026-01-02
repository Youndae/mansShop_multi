package com.example.moduleapi.config.exception;

import com.example.modulecommon.customException.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn("HandlerMethodValidationException::message : {}", e.getMessage());
        log.warn("HandlerMethodValidationException::AllErrors : {}", e.getAllErrors());

        List<ExceptionEntity> resBody = e.getAllErrors().stream()
                .map(v -> toValidationExceptionEntity(ErrorCode.BAD_REQUEST, v.getDefaultMessage()))
                .toList();

        return toValidationResponseEntity(ErrorCode.BAD_REQUEST, resBody);
    }

    private ExceptionEntity toValidationExceptionEntity(ErrorCode errorCode, String message) {
        return new ExceptionEntity(
                errorCode.getHttpStatus().value(),
                message
        );
    }

    private ResponseEntity<Object> toValidationResponseEntity(ErrorCode errorCode, List<ExceptionEntity> exceptionEntities) {

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        exceptionEntities
                );
    }

    @ExceptionHandler(CustomTokenExpiredException.class)
    public ResponseEntity<ExceptionEntity> tokenExpiredException(Exception e) {
        log.warn("tokenExpiredException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.TOKEN_EXPIRED);
    }

    @ExceptionHandler({CustomAccessDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ExceptionEntity> accessDeniedException(Exception e) {
        log.warn("AccessDeniedException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(CustomTokenStealingException.class)
    public ResponseEntity<ExceptionEntity> tokenStealingException(Exception e) {
        log.warn("TokenStealing : {}", e.getMessage());

        return toResponseEntity(ErrorCode.TOKEN_STEALING);
    }

    @ExceptionHandler({
            CustomBadCredentialsException.class,
            BadCredentialsException.class,
            InternalAuthenticationServiceException.class
    })
    public ResponseEntity<ExceptionEntity> badCredentialsException(Exception e) {
        log.warn("BadCredentials Exception : {}", e.getMessage());

        return toResponseEntity(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler({CustomNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<ExceptionEntity> notFoundException(Exception e) {
        log.warn("NotFoundException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionEntity> nullPointerException(Exception e) {
        log.warn("nullPointerException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomOrderSessionExpiredException.class)
    public ResponseEntity<ExceptionEntity> orderSessionExpiredException(Exception e) {
        log.warn("orderSessionExpiredException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.ORDER_SESSION_EXPIRED);
    }

    @ExceptionHandler(CustomOrderDataFailedException.class)
    public ResponseEntity<ExceptionEntity> orderDataFailedException(Exception e) {
        log.warn("orderDataFailedException : {}", e.getMessage());

        return toResponseEntity(ErrorCode.ORDER_DATA_FAILED);
    }

    @ExceptionHandler({CannotCreateTransactionException.class, JpaSystemException.class, SQLException.class})
    public ResponseEntity<ExceptionEntity> cannotCreateTransactionException(Exception e) {
        log.warn("DB Connection Exception : {}", e.getMessage());

        return toResponseEntity(ErrorCode.DB_CONNECTION_FAILED);
    }

    @ExceptionHandler({CustomDuplicateException.class, CustomConflictException.class})
    public ResponseEntity<ExceptionEntity> duplicateException(Exception e) {
        loggingConflict(e);

        return toResponseEntity(ErrorCode.CONFLICT);
    }

    private void loggingConflict(Exception e) {
        if(e instanceof CustomConflictException)
            log.error("conflictException : {}", e.getMessage());
        else
            log.info("data Duplicate Exception: {}", e.getMessage());
    }

    private ResponseEntity<ExceptionEntity> toResponseEntity(ErrorCode errorCode) {

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(
                        new ExceptionEntity(
                                errorCode.getHttpStatus().value(),
                                errorCode.getMessage()
                        )
                );
    }
}
