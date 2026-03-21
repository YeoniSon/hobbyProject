package com.example.user.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 응답 — 이메일 인증용 토큰만 포함 (전체 URL 아님).
 */
@Getter
@Builder
public class SignupResponse {

    private String token;

   public static SignupResponse of(String token) {
       return SignupResponse.builder()
               .token(token)
               .build();
   }
}
