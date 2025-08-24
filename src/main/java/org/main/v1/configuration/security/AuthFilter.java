package org.main.v1.configuration.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.Claims;
import org.main.essentials.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private static final String DOMAIN = "@chilesotti.it";
    private final DataSource dataSource;
    private final Gson gson = new GsonBuilder().create();
    private final JwtUtil jwtUtil;

    public AuthFilter(DataSource dataSource, JwtUtil jwtUtil) {
        this.dataSource = dataSource;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.equals("/api/v1/users/login") || path.equals("/api/v1/users/create") || path.equals("/api/v1/test") || path.equals("/api/v1/users/check") || path.equals("/api/v1/users/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Ottieni header e parametri necessari alla verifica
        String authToken = request.getHeader("Authorization");
        String username = request.getParameter("author");
        String role = getRole(username);

        // Verifica la validità del token
        if (authToken == null || jwtUtil.validateToken(authToken.replace("Bearer ", "").trim().replaceAll("\\s+", "")).getSubject() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing or Invalid Token");
            return;
        }
        
        if (path.equals("/sportello/all") || path.equals("/users/all") || path.matches("^\\/\\d+\\/remove-subscription\\/[a-zA-Z0-9_]+$") || path.matches("^/materia/.+\n") || path.matches("^/aula/.+\n") ) {
            if (!role.equals("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.matches("/users/\\w+")) {
            if (username.equals(path.split("/")[2]) || role.equals("ADMIN")) {
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.matches("/users/modify/\\w+") || path.matches("/users/remove/\\w+")) {
            /*
            * CONTROLLO FATTO ALL'INTERNO DEL CONTROLLER IN CASO ECCEZZIONALE A CAUSA DI PROBLEMI DI GESTIONE DELLO STREAM DI DATI
            * 
            * QUI È SUFFICENTE VERIFICARE L'AUTENTICITÀ DI COLUI CHE EFFETTUA LA RICHIESTA
            * */
        } else if (path.matches("/sportello/by/\\w+")) {
            String teacherName = path.split("/")[3];
            if (teacherName.equals(username) && !role.equals("STUDENT")) {
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else if (path.matches("/sportello/subscribe/\\w+") || path.matches("/sportello/unsubscribe/\\w+") || path.matches("/sportello/subscribed")) {
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
            String teacherName = getDocenteResponsabileForSportello(Long.parseLong(path.split("/")[3])).split("@")[0];
            
            boolean isAuthorized = (teacherName.equals(username) && role.equals("TEACHER")) || role.equals("ADMIN");
            if (!isAuthorized) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Se il token è valido, prosegui con la catena di filtri
        filterChain.doFilter(request, response);
    }

    // Logica per verificare la validità del token
    private boolean isValidAuthToken(String username, String token) {
        try (Connection connection = dataSource.getConnection();  // <-- Modifica qui
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT 1 FROM auth_token WHERE user_id = ? AND token = ? AND expires_at > ?")) {
            
            statement.setString(1, username + DOMAIN);
            statement.setString(2, token);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            logger.error("Error validating token", e);
            return false;
        }
    }

    private String getDocenteResponsabileForSportello(long id) {
        try (Connection connection = dataSource.getConnection();  // <-- Modifica qui
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT docente_responsabile FROM sportello WHERE id_sportello = ?")) {
            
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("docente_responsabile") : "";
            }
        } catch (SQLException e) {
            logger.error("Error fetching docente", e);
            return "";
        }
    }

    private String getRole(String username) {
        try (Connection connection = dataSource.getConnection();  // <-- Modifica qui
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT ruolo FROM persona WHERE email = ?")) {
            
            statement.setString(1, username + DOMAIN);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("ruolo") : "";
            }
        } catch (SQLException e) {
            logger.error("Error fetching role", e);
            return "";
        }
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

}
