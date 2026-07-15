package com.colegio.gestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gestionEscolarOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Sistema de Gestion Escolar - API")
                .description("API REST para administrar estudiantes, profesores y cursos de un colegio")
                .version("1.0.0")
                .contact(new Contact().email("jevojob@gmail.com")));
    }
}
