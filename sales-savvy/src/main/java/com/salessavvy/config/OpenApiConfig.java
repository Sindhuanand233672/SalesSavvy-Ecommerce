package com.salessavvy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salesSavvyOpenAPI() {

        return new OpenAPI()

            .info(new Info()
                .title("SalesSavvy E-Commerce API")
                .description(
                    "REST API documentation for the SalesSavvy " +
                    "E-Commerce application. Includes authentication, " +
                    "products, categories, cart, wishlist, orders, " +
                    "payments, reviews, inventory, addresses and coupons."
                )
                .version("1.0.0")
                .contact(new Contact()
                    .name("SalesSavvy Development Team")
                )
            )

            .components(new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter your JWT token")
                )
            );
    }
}