package ru.otus.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8005}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://delivery.arch.homework").description("Production Server"),
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")
                ))
                .info(new Info().title("Delivery Management API").version("1.0")
                        .description("API for delivery slot reservation"));
    }
}