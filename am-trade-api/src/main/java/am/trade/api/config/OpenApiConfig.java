package am.trade.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI documentation
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI amTradeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AM Trade Management API")
                        .description("API documentation for AM Trade Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AM Trade Team")
                                .email("support@amtrade.com")
                                .url("https://www.amtrade.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addServersItem(new Server()
                        .url("/")
                        .description("Default Server URL"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication. Enter the JWT token in the format: Bearer {token}")));
    }
}
