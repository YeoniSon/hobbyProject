package com.example.api.config;

import com.example.api.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of("*"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("Authorization"));
        c.setAllowCredentials(false);
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    /**
     * 회원가입·로그인 등: 이 체인이 먼저 매칭되면 JWT 필터 없이 전부 permitAll.
     * authorizeHttpRequests 만으로는 403 이 나는 환경 대응.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain fullyPublicApiChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.securityMatcher(PathWithinApplicationRequestMatchers.fullyPublicPathMatcher());
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(100)
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        var authPatchUser = PathWithinApplicationRequestMatchers.matching(HttpMethod.PATCH,
                "/users/deposit",
                "/users/withdraw",
                "/users/profile/edit",
                "/users/change-password");
        var authGetProfile = PathWithinApplicationRequestMatchers.matching(HttpMethod.GET,
                "/users/profile");

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/chat-demo.html",
                                "/webjars/**",
                                "/h2-console/**",
                                "/ws", "/ws/**"
                        ).permitAll()
                        .requestMatchers(authPatchUser).authenticated()
                        .requestMatchers(authGetProfile).authenticated()
                        .requestMatchers(
                                "/category/register",
                                "/post/upload",
                                "/post/my-posts",
                                "/post/{postId}/detail",
                                "/comment/upload",
                                "/comment/{userId}/all-comments",
                                "/comment/{postId}/all-comments",
                                "/like/**",
                                "/report/post/{postId}",
                                "/report/comment/{commentId}",
                                "/report/all-reports",
                                "/report/details/{reportId}",
                                "/report/delete/{reportId}",
                                "/report/count/**",
                                "/chat/**"
                        ).authenticated()
                        .requestMatchers(
                                "/category/manage/**",
                                "/post/manage/**",
                                "/admin/manage/**",
                                "/comment/manage/**",
                                "/notice/manage/**",
                                "/report/manage/**"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
