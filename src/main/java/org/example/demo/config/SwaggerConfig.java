package org.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Evaluation Service API")
                        .version("v1.0")
                        .description("APIs for evaluating CSVs and exporting results")
                        .contact(new Contact()
                                .name("Pratik Dimble")
                                .email("pratik.dimble@nitorinfotech.com")
                                .url("http://localhost:8080/swagger-ui/index.html"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}

