package com.exemplo.apiaula.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiDocumentacao() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Tarefas")
                        .version("1.0")
                        .description("API para cadastro de usuários e gerenciamento de tarefas."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("UUID")
                                        .description("Informe o token recebido no login.")));
    }
}
