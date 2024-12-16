package com.example.moduleconfig.config.exception.customException;

import com.example.modulecommon.model.enumuration.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomTokenStealingException extends RuntimeException{
    ErrorCode errorCode;

    String message;
}
