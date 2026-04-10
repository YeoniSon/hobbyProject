package com.example.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String sercuritySchemeName = "JWTAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        /* 전역 addSecurityItem 미사용: 공개 API에도 Bearer 가 붙는 것을 막음. JWT 필요 시 Authorize 후 해당 엔드포인트만 스키마 적용. */
        return new OpenAPI()
                /* 상대 경로: Swagger 를 연 호스트·포트로 요청 (localhost vs 127.0.0.1 혼용 시 불필요한 CORS/403 방지) */
                .servers(List.of(new Server().url("/").description("현재 문서와 동일 출처")))
                .info(new Info()
                        .title("HobbyProject API")
                        .description("""
                                취미 플랫폼 REST API입니다.
                                - 공개 API: Authorize 없이 호출 가능합니다.
                                - 보호 API: 우측 Authorize에 Bearer {JWT} 형식으로 토큰을 넣은 뒤 호출합니다.
                                - WebSocket(STOMP) 채팅은 이 HTTP 문서와 별도입니다.
                                """)
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes(sercuritySchemeName, securityScheme));
    }
}
