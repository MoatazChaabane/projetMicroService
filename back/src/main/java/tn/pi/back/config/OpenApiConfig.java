package tn.pi.back.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "cookieAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("API Gestion Profil - Application Médicale")
                        .version("1.0.0")
                        .description("API REST pour la gestion des profils utilisateurs dans une application médicale. " +
                                "Supporte les rôles PATIENT, DOCTOR et ADMIN.")
                        .contact(new Contact()
                                .name("Support API")
                                .email("support@medicalapp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("JSESSIONID")
                                        .description("Authentification par session (cookie JSESSIONID). Connectez-vous via /api/auth/login pour obtenir le cookie.")));
    }
}

