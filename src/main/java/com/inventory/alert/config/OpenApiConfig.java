package com.inventory.alert.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventory Alert Management API",
                version = "v1",
                description = "REST API for inventory monitoring and alert management"
        )
)
public class OpenApiConfig {
}
