package com.inventory.alert.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventory Alert Management API",
                version = "v1",
                description = "REST API for product catalog, stock mutations, and low-stock alerts. "
                        + "Use Authorize with a Bearer JWT from POST /api/v1/auth/login.",
                contact = @Contact(name = "Inventory Alert Team", email = "support@inventory-alert.local")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        },
        tags = {
                @Tag(name = "Products", description = "Product catalog operations"),
                @Tag(name = "Inventory", description = "Stock purchase, sale, and history"),
                @Tag(name = "Alerts", description = "Low-stock alert lifecycle"),
                @Tag(name = "Authentication", description = "Login and registration (public)")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste the accessToken from /api/v1/auth/login"
)
public class OpenApiConfig {
}
