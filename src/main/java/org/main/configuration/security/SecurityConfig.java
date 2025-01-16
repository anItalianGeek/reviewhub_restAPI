package org.main.configuration.security;

import com.mysql.cj.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .addFilterBefore(new AuthFilter(), UsernamePasswordAuthenticationFilter.class) // Filtro personalizzato
                .csrf(csrf -> csrf.disable()) // Disabilitazione CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable()) // Disabilita formLogin
                .httpBasic(basic -> basic.disable()) // Disabilita httpBasic
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/login", "/users/create", "/test/**", "/users/check").permitAll() // Questi endpoint sono accessibili senza autenticazione
                        .anyRequest().authenticated() // Tutti gli altri endpoint richiedono autenticazione
                )
                .build();
    }
    
    
}
