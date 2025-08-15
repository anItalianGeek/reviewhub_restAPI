package org.main.v1.configuration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final AuthFilter authFilter;

    public SecurityConfig(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /**
         * IMPORTANTE: PER QUALCHE MOTIVO, ANCORA SCONOSCIUTO, ANCHE SE GLI ENDPOINT VENGONO CONSIDERATI 
         * ACCESSIBILI PUBBLICAMENTE SENZA AUTENTICAZIONE, IN REALTÀ L'AUTENTICAZIONE SERVCE ECCOME, DIFATTI
         * TUTTE LE RICHIESTE SENZA Authorization VERRANNO RIFIUTATE
         * */
        return http
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class) // Filtro personalizzato
                .csrf(csrf -> csrf.disable()) // Disabilitazione CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable()) // Disabilita formLogin
                .httpBasic(basic -> basic.disable()) // Disabilita httpBasic
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/login", "/users/create", "test/**", "/users/check", "/**").permitAll() // Questi endpoint sono accessibili senza autenticazione
                        .anyRequest().authenticated() // Tutti gli altri endpoint richiedono autenticazione
                )
                .build();
    }
    
}
