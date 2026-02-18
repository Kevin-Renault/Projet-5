package com.openclassrooms.mddapi.config;

import com.openclassrooms.mddapi.security.JwtCookieService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";
    public static final String COOKIE_AUTH_SCHEME = "cookieAuth";

    @Bean
    public OpenAPI openAPI(
            @Value("${spring.application.name:mdd-api}") String applicationName,
            JwtCookieService cookieService) {

        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste a JWT access token (without the 'Bearer ' prefix).");

        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name(cookieService.getCookieName())
                .description("JWT access token cookie.");

        return new OpenAPI()
                .info(new Info().title(applicationName).version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, bearerAuth)
                        .addSecuritySchemes(COOKIE_AUTH_SCHEME, cookieAuth));
    }
}
