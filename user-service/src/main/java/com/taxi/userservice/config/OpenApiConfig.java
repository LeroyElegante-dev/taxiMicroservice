package com.taxi.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Taxi — User Service")
                        .version("1.0")
                        .description("REST API пассажиров и водителей (этап 1). Кнопка Authorize: вставьте JWT из ответа /auth/login."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT))
                .components(new Components().addSecuritySchemes(BEARER_JWT,
                        new SecurityScheme()
                                .name(BEARER_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Только значение токена (Swagger сам добавит префикс Bearer).")));
    }
}
