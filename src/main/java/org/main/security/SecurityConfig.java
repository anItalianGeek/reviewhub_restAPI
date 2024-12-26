package org.main.security;

import org.main.controllers.repositories.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private PersonaRepository personaRepository;
    
    @Autowired
    public SecurityConfig(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .requestMatchers("/test/**").authenticated()  // Protegge gli endpoint "/test/**"
                .anyRequest().permitAll()  // Permette tutte le altre richieste
                .and()
                .addFilterBefore(new AuthFilter(personaRepository), UsernamePasswordAuthenticationFilter.class);  // Aggiungi il filtro prima di UsernamePasswordAuthenticationFilter
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> loggingFilter() {
        FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthFilter(personaRepository));
        registrationBean.addUrlPatterns("/test/*");  // Applica il filtro agli endpoint "/test/*"
        return registrationBean;
    }
}
