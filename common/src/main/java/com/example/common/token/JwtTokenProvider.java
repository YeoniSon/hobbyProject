package com.example.common.token;

public interface JwtTokenProvider {
    String createToken(Long userId);

    boolean validate(String token);

    Long getUserId(String token);
}
