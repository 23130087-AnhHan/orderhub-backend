package com.orderhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI orderHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OrderHub Backend API")
                        .description("""
                                OrderHub Backend System is a Spring Boot backend project for e-commerce order management.
                                
                                Main modules:
                                - Authentication with JWT
                                - Product and category management
                                - Shopping cart
                                - Order management
                                - Fake payment
                                - Event-driven notifications with RabbitMQ
                                - Notification storage with MongoDB
                                - Product cache with Redis
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("OrderHub Backend")
                                .email("your-email@example.com"))
                        .license(new License()
                                .name("Portfolio Project")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}