package org.main.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.aspectj.weaver.patterns.PerObject;
import org.main.controllers.repositories.PersonaRepository;
import org.main.models.Persona;
import org.main.models.Sportello;
import org.main.models.UserIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class AuthFilter extends OncePerRequestFilter {

    private static final String DOMAIN = "@chilesotti.it";
    private PersonaRepository personaRepository;
    
    public AuthFilter(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Ottieni l'header di autenticazione
        String authToken = request.getHeader("Authorization");
        String username = request.getParameter("author");
        String role = getRole(username);
        String path = request.getRequestURI();
        
        if (path.equals("/users/login") || path.equals("/test")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Verifica la validità del token
        if (authToken == null || !isValidAuthToken(username, authToken.replace("Bearer ", ""))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid Token");
            return;
        }
        
        Gson gson = new GsonBuilder().create();
        if (path.equals("/sportello/all") || path.equals("/users/all")) {
            if (!role.equals("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.matches("/users/\\w+")) {
            if (!role.equals("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.equals("/users/create") || path.matches("/users/modify/\\w+") || path.matches("/users/remove/\\w+")) {
            String newUsername = gson.fromJson(getRequestBody(request), Persona.class).getEmail().split("@")[0];
            if (!role.equals("ADMIN") || !newUsername.equals(username)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.matches("/sportello/by/\\w+")) {
            String teacherName = path.split("/")[3];
            if (role.equals("STUDENT") || !teacherName.equals(username)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.matches("/sportello/subscribe/\\w+") || path.matches("/sportello/unsubscribe/\\w+")) {
            if (!role.equals("STUDENT")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.equals("/sportello/create")) {
            if (role.equals("STUDENT")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.matches("/sportello/modify/\\w+") || path.matches("/sportello/remove/\\w+")) {
            String teacherName = gson.fromJson(getRequestBody(request), Sportello.class).getDocente_responsabile().getEmail().split("@")[0];
            if (role.equals("STUDENT") || !teacherName.equals(username)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Se il token è valido, prosegui con la catena di filtri
        filterChain.doFilter(request, response);
    }

    // Logica per verificare la validità del token
    private boolean isValidAuthToken(String username, String token) {
        return personaRepository.verificaCodice(username + DOMAIN, token);
    }
    
    private String getRole(String username){
        return String.valueOf(personaRepository.ottieniRuolo(username + DOMAIN));
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
