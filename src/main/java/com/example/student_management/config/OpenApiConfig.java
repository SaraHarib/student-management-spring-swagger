package com.example.student_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Student Management API")
                .version("1.0")
                .description("TP6 — Gestion des étudiants avec Spring Boot, JPA et MySQL/MariaDB. "
                        + "CRUD et statistiques par année de naissance."));
    }
}
