package com.example.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 200, "잘못된 요청입니다."),
    // TOKEN 에러
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, 200, "유효하지 않는 토큰입니다."),
    ALREADY_USED_TOKEN(HttpStatus.BAD_REQUEST, 200, "이미 사용이 완료된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, 200, "만료된 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, 200, "유효하지 않는 토큰타입 입니다."),
    UNUSED_EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, 200, "인증되지 않았거나 만료된 토큰 입니다."),
    // 회원 관련
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, 200, "맞지 않는 토큰입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 전화번호입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 닉네임입니다."),
    NO_CHANGE(HttpStatus.BAD_REQUEST, 200, "수정할 내용이 기존 정보와 동일합니다."),
    //로그인
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 200, "비밀번호가 일치하지 않습니다."),
    NOT_VERIFIED(HttpStatus.BAD_REQUEST, 200,  "인증이 되지 않았습니다."),
    WITHDRAW_EMAIL(HttpStatus.BAD_REQUEST, 200,  "탈퇴된 계정입니다."),

    //비밀번호 변경
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, 200, "기존 비밀번호와 동일합니다."),
    NOT_EXIST_EMAIL(HttpStatus.BAD_REQUEST, 200, "존재하지 않은 이메일입니다."),

    // 회원관리 관련
    NOT_CHANGEABLE_ROLE(HttpStatus.BAD_REQUEST, 200, "권한 변경 불가합니다."),

    //Board 부분
    // 카테고리 관련
    DUPLICATE_CATEGORY(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 카테고리입니다."),
    NOT_EXIST_CATEGORY(HttpStatus.BAD_REQUEST, 200, "존재하지 않는 카테고리입니다."),
    ALREADY_DELETE_CATEGORY(HttpStatus.BAD_REQUEST, 200, "이미 삭제된 카테고리입니다."),
    ALREADY_SHOW_CATEGORY(HttpStatus.BAD_REQUEST, 200, "이미 공개되어있는 카테고리입니다."),

    // Post 관련
    NOT_EXIST_POST(HttpStatus.BAD_REQUEST, 200, "존재하지 않는 게시글입니다."),
    NOT_MATCH_WRITER(HttpStatus.BAD_REQUEST, 200, "작성자가 맞지 않습니다."),

    // Notice 관련
    DUPLICATE_NOTICE(HttpStatus.BAD_REQUEST, 200, "이미 존재하는 공지사항입니다."),
    NOT_EXIST_NOTICE(HttpStatus.BAD_REQUEST, 200, "존재하지 않는 공지사항입니다."),

    // 댓글 관련
    NOT_EXIST_COMMENT(HttpStatus.BAD_REQUEST,200 , "존재하지 않는 댓글입니다."),

    // 좋아요 관련
    ALREADY_EXIST_LIKE(HttpStatus.BAD_REQUEST,200, "이미 좋아요를 눌렀습니다."),
    NOT_EXIST_LIKE(HttpStatus.BAD_REQUEST, 200, "좋아요가 없습니다."),

    //신고 관련
    ALREADY_EXIST_REPORT(HttpStatus.BAD_REQUEST,200 ,"이미 신고했습니다."),
    NOT_EXIST_REPORT(HttpStatus.BAD_REQUEST,200 ,"신고 내용이 없습니다."),
    NOT_ENOUGH_REPORTS_FOR_PRIVATE(HttpStatus.BAD_REQUEST, 200, "신고가 20건 이상일 때만 비공개 처리할 수 있습니다."),
    
    // Chat 관련
    INVALID_REQUEST_CHAT_ROOM(HttpStatus.BAD_REQUEST, 200, "채팅 상대가 존재하지 않습니다."),
    NOT_EXIST_ROOM(HttpStatus.BAD_REQUEST,200 , "채팅방이 존재하지 않습니다.");


    private final HttpStatus httpStatus;
    private final int status;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int status, String msg) {
        this.httpStatus = httpStatus;
        this.status = status;
        this.message = msg;
    }
}
