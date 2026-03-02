package com.example.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String sercuritySchemeName = "JWTAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("HabitProject API")
                        .description("취미 플랫폼 (개인 프로젝트) API")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes(sercuritySchemeName, securityScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList(sercuritySchemeName));
    }
}
