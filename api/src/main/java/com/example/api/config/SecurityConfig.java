package com.example.api.config;

import com.example.api.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // RESET API니까 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
				// H2 콘솔용 frame 허용
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                //기본 로그인 폼 비활성화
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //허용 범위
                .authorizeHttpRequests(auth -> auth
                        // swagger 전부 오픈
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
								"/swagger-ui.html",
                                // h2-console 접근 허용
								"/h2-console/**",
                                // 회원가입·이메일 인증·로그인만 비인증 허용
                                "/users/signup",
                                "/users/email-verify",
                                "/users/login",
                                // 비밀번호 재설정(로그인 없이 접근)
                                "/users/reset-password",
                                "/users/reset-password/email-verify",
                                "/users/reset-password/change-password",
                                // 관리자 계정 등록
                                "/admin/register",
                                //게시글 관련
                                "/post/upload",
                                "/post/my-posts",
                                "/post/{postId}/detail"
                        ).permitAll()

                        // 프로필 조회/수정은 인증 필요
                        .requestMatchers(
                                "/users/deposit",
                                "/users/withdraw",
                                "/users/profile",
                                "/users/profile/edit",
                                "/users/change-password",

                                // 카테고리 등록은 인증 필요
                                "/category/register"
                        ).authenticated()

                        // 관리자 전용 (ADMIN 역할 필요)
                        .requestMatchers(
                                //카테고리 관리
                                "/category/manage/**",
                                // 게시글 관리
                                "/post/manage/**",

                                // 계정 관리
                                "/admin/manage/**"

                        ).hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
