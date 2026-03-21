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
                        .description("취미 플랫폼 (개인 프로젝트) API")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes(sercuritySchemeName, securityScheme));
    }
}
