package com.example.modulecommon.customException;


import com.example.modulecommon.model.enumuration.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomConflictException extends RuntimeException {
    ErrorCode errorCode;

    String message;
}
