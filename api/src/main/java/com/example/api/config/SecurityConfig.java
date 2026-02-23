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
                                "/users/login"
                        ).permitAll()
                        // 프로필 조회/수정은 인증 필요
                        .requestMatchers("/users/profile", "/users/profile/edit").authenticated()
                        // 그 외 /users 하위는 인증 필요
                        .requestMatchers("/users/**").authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
