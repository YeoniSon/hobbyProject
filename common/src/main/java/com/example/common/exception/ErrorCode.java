package com.example.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 200, "잘못된 요청입니다."),
    // TOKEN 에러
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, 200, "토큰이 존재하지 않습니다."),
    ALREADY_USED_TOKEN(HttpStatus.BAD_REQUEST, 200, "이미 사용이 완료된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, 200, "만료된 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, 200, "맞지 않는 토큰입니다."),
    // 회원 관련
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, 200, "맞지 않는 토큰입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 전화번호입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 닉네임입니다."),
    NO_CHANGE(HttpStatus.BAD_REQUEST, 200, "수정할 내용이 기존 정보와 동일합니다."),
    //로그인
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 200, "비밀번호가 일치하지 않습니다."),
    NOT_VERIFIED(HttpStatus.BAD_REQUEST, 200,  "인증이 되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final int status;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int status, String msg) {
        this.httpStatus = httpStatus;
        this.status = status;
        this.message = msg;
    }
}
