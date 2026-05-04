package com.taxi.tripservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tripServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Taxi — Trip Service")
                        .version("1.0")
                        .description("Поездки, назначение водителей, расчёт цены (этап 2)"));
    }
}
