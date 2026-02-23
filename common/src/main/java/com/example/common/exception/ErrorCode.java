package com.example.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 200, "잘못된 요청입니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, 200, "토큰이 존재하지 않습니다."),
    ALREADY_USED_TOKEN(HttpStatus.BAD_REQUEST, 200, "이미 사용이 완료된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, 200, "만료된 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, 200, "맞지 않는 토큰입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 이메일입니다.");

    private final HttpStatus httpStatus;
    private final int status;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int status, String msg) {
        this.httpStatus = httpStatus;
        this.status = status;
        this.message = msg;
    }
}
