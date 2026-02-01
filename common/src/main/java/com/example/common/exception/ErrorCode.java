package com.example.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 200, "잘못된 요청입니다.");

    private final HttpStatus httpStatus;
    private final int status;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int status, String msg) {
        this.httpStatus = httpStatus;
        this.status = status;
        this.message = msg;
    }
}
