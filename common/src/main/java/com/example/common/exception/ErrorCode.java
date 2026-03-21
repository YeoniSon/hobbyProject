package com.example.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    // TOKEN 에러
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않는 토큰입니다."),
    ALREADY_USED_TOKEN(HttpStatus.CONFLICT, "이미 사용이 완료된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "유효하지 않는 토큰타입 입니다."),
    UNUSED_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "인증되지 않았거나 만료된 토큰 입니다."),
    // 회원 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 존재하는 전화번호입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    /** DB 유니크 제약 등 (사전 검증·동시 가입 레이스 등) */
    DUPLICATE_DATA(HttpStatus.CONFLICT, "이미 사용 중인 값입니다."),
    NO_CHANGE(HttpStatus.BAD_REQUEST, "수정할 내용이 기존 정보와 동일합니다."),
    //로그인
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    NOT_VERIFIED(HttpStatus.FORBIDDEN, "인증이 되지 않았습니다."),
    WITHDRAW_EMAIL(HttpStatus.FORBIDDEN, "탈퇴된 계정입니다."),

    //비밀번호 변경
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호와 동일합니다."),
    NOT_EXIST_EMAIL(HttpStatus.NOT_FOUND, "존재하지 않은 이메일입니다."),

    // 회원관리 관련
    NOT_CHANGEABLE_ROLE(HttpStatus.FORBIDDEN, "권한 변경 불가합니다."),

    //Board 부분
    // 카테고리 관련
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "이미 존재하는 카테고리입니다."),
    NOT_EXIST_CATEGORY(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    ALREADY_DELETE_CATEGORY(HttpStatus.GONE, "이미 삭제된 카테고리입니다."),
    ALREADY_SHOW_CATEGORY(HttpStatus.BAD_REQUEST, "이미 공개되어있는 카테고리입니다."),

    // Post 관련
    NOT_EXIST_POST(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    NOT_MATCH_WRITER(HttpStatus.FORBIDDEN, "작성자가 맞지 않습니다."),

    // Notice 관련
    DUPLICATE_NOTICE(HttpStatus.CONFLICT, "이미 존재하는 공지사항입니다."),
    NOT_EXIST_NOTICE(HttpStatus.NOT_FOUND, "존재하지 않는 공지사항입니다."),

    // 댓글 관련
    NOT_EXIST_COMMENT(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),

    // 좋아요 관련
    ALREADY_EXIST_LIKE(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다."),
    NOT_EXIST_LIKE(HttpStatus.NOT_FOUND, "좋아요가 없습니다."),

    //신고 관련
    ALREADY_EXIST_REPORT(HttpStatus.CONFLICT,"이미 신고했습니다."),
    NOT_EXIST_REPORT(HttpStatus.NOT_FOUND,"신고 내용이 없습니다."),
    NOT_ENOUGH_REPORTS_FOR_PRIVATE(HttpStatus.FORBIDDEN, "신고가 20건 이상일 때만 비공개 처리할 수 있습니다."),
    
    // Chat 관련
    INVALID_REQUEST_CHAT_ROOM(HttpStatus.NOT_FOUND, "채팅 상대가 존재하지 않습니다."),
    NOT_EXIST_ROOM(HttpStatus.NOT_FOUND, "채팅방이 존재하지 않습니다."),
    NOT_EXIST_MESSAGE(HttpStatus.NOT_FOUND, "메시지가 존재하지 않습니다."),
    MESSAGE_DELETE_TIME_EXPIRED(HttpStatus.FORBIDDEN, "메시지는 전송 후 10분 이내에만 삭제할 수 있습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String msg) {
        this.httpStatus = httpStatus;
        this.message = msg;
    }

    public int getStatus() {
        return httpStatus.value();
    }
}
