package com.example.moduleapi.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addParameters("Authorization",
                                new Parameter()
                                        .in(ParameterIn.HEADER.toString())
                                        .required(true)
                                        .name("Authorization")
                                        .description("AccessToken Header")
                                        .schema(new StringSchema())
                        )
                        .addParameters("RefreshToken",
                                new Parameter()
                                        .in(ParameterIn.COOKIE.toString())
                                        .required(true)
                                        .name("RefreshToken")
                                        .description("RefreshToken Cookie")
                                        .schema(new StringSchema())
                        )
                        .addParameters("ino",
                                new Parameter()
                                        .in(ParameterIn.COOKIE.toString())
                                        .required(true)
                                        .name("ino")
                                        .description("ino Cookie")
                                        .schema(new StringSchema())
                        )
                )
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Man's Shop Application API")
                .description("Man's Shop API")
                .version("1.0.0");
    }
}
