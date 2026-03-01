package com.example.api.config;

import com.example.api.security.CustomUserDetails;
import com.example.common.token.JwtTokenProvider;
import com.example.user.repository.UserRepository;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 핸드셰이크 시 쿼리 파라미터 token으로 JWT 검증 후 Principal 설정.
 * 클라이언트 연결: /ws?token=엑세스토큰
 */
@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtHandshakeHandler(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected Principal determineUser(
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.web.socket.WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return null;
        }
        String token = servletRequest.getServletRequest().getParameter("token");
        if (token == null || token.isBlank() || !jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or missing token");
        }
        Long userId = jwtTokenProvider.getUserId(token);
        return userRepository.findById(userId)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
