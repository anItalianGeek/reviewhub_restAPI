package org.main.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*"); // Permetti tutte le entrate
        config.addAllowedMethod("*"); // Permetti tutti i metodi (GET, POST, PUT, DELETE, ecc.)
        config.addAllowedHeader("*"); // Permetti tutte le intestazioni
        config.setAllowCredentials(true); // Consenti l'invio di cookie o token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
