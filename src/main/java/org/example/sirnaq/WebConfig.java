package org.example.sirnaq;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Wszystkie endpointy
                .allowedOrigins("*") // Zwolenie na wszystkie pochodzenia dla testów
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Dozwolone metody
                .allowedHeaders("*"); //Dozwolone wszystkie nagłówki
    }
}
