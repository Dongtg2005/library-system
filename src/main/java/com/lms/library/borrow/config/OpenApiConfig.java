package com.lms.library.borrow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI borrowServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Borrow Service API")
                        .description("API Documentation for Library Management System - Borrow Service")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("LMS Support")
                                .email("support@lms.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Local Service Development Port"),
                        new Server().url("http://localhost:8000/borrow-service").description("API Gateway Port")
                ));
    }
}
