package com.inventory.alert.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventory Alert Management API",
                version = "v1",
                description = "REST API for product catalog, stock mutations, and low-stock alerts. "
                        + "JWT authentication is planned for a later phase; endpoints are currently open.",
                contact = @Contact(name = "Inventory Alert Team", email = "support@inventory-alert.local")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local")
        },
        tags = {
                @Tag(name = "Products", description = "Product catalog operations"),
                @Tag(name = "Inventory", description = "Stock purchase, sale, and history"),
                @Tag(name = "Alerts", description = "Low-stock alert lifecycle"),
                @Tag(name = "Authentication", description = "Auth contract (JWT placeholder)")
        }
)
public class OpenApiConfig {

    @Bean
    OpenAPI inventoryOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("PHASE 9: send Authorization: Bearer <token>")));
    }
}
