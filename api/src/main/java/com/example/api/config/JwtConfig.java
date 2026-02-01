package com.example.api.config;

import com.example.api.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(secretKey, accessTokenExpiration);
    }
}
