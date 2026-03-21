package com.example.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_ENTRY = Pattern.compile("Duplicate entry '([^']*)'");

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponse(e.getErrorCode()));
    }

    /**
     * 사전 검증을 통과해도 DB 유니크 제약에 걸리는 경우(레이스, 배포 전 JAR 등) 500 대신 409 로 응답.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException e) {
        String msg = e.getMostSpecificCause() != null
                ? e.getMostSpecificCause().getMessage()
                : e.getMessage();
        ErrorCode code = mapMysqlDuplicate(msg);
        return ResponseEntity.status(code.getStatus()).body(new ErrorResponse(code));
    }

    private static ErrorCode mapMysqlDuplicate(String message) {
        if (message == null || !message.contains("Duplicate entry")) {
            return ErrorCode.DUPLICATE_DATA;
        }
        Matcher m = DUPLICATE_ENTRY.matcher(message);
        if (!m.find()) {
            return ErrorCode.DUPLICATE_DATA;
        }
        String value = m.group(1);
        if (value.contains("@")) {
            return ErrorCode.DUPLICATE_EMAIL;
        }
        /* 한국 휴대폰 번호 형태 (010-xxxx-xxxx 등) */
        if (value.matches("0[0-9\\-]{8,20}")) {
            return ErrorCode.DUPLICATE_PHONE;
        }
        return ErrorCode.DUPLICATE_DATA;
    }
}
