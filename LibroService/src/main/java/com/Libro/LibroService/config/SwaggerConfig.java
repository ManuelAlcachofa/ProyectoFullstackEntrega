package com.Libro.LibroService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API 2026 Registro de Libros")
                        .version("1.0")
                        .description("Documentación de la API para el sistema de libros")
                        .contact(new Contact()
                                .name("Equipo Proyecto Biblioteca"))
                        .license(new License()
                                .name("Uso académico - Duoc UC")))
                .servers(List.of(new Server()
                        .url("http://localhost:9092")
                        .description("Servidor local del microservicio")));
    }
}
