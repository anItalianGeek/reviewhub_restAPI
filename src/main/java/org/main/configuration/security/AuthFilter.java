package org.main.configuration.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.main.models.Persona;
import org.main.models.Sportello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class AuthFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/reviewhub_db?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true";
    private static final String username = "admin";
    private static final String password = "admin";
    private static Connection connection;
    private static final Semaphore mutex = new Semaphore(1);
    private static final String DOMAIN = "@chilesotti.it";
    
    public AuthFilter() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.equals("/users/login") || path.equals("/users/create") || path.equals("/test") || path.equals("/users/check")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Ottieni header e parametri necessari alla verifica
        String authToken = request.getHeader("Authorization");
        String username = request.getParameter("author");
        String role = getRole(username);
        
        // Verifica la validità del token
        if (authToken == null || !isValidAuthToken(username, authToken.replace("Bearer ", ""))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing or Invalid Token");
            return;
        }
        
        Gson gson = new GsonBuilder().create();
        if (path.equals("/sportello/all") || path.equals("/users/all") || path.matches("^\\/\\d+\\/remove-subscription\\/[a-zA-Z0-9_]+$")) {
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
            String newUsername = gson.fromJson(getRequestBody(request), Persona.class).getEmail().split("@")[0];
            if (newUsername.equals(username) || role.equals("ADMIN")) {
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
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
            String teacherName = gson.fromJson(getRequestBody(request), Sportello.class).getDocente_responsabile().getEmail().split("@")[0];
            if (teacherName.equals(username) && !role.equals("STUDENT")) {
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: Insufficient Permissions");
                return;
            }
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("authentication > " + SecurityContextHolder.getContext().getAuthentication());
        // Se il token è valido, prosegui con la catena di filtri
        filterChain.doFilter(request, response);
    }

    // Logica per verificare la validità del token
    private boolean isValidAuthToken(String username, String token) {
        try {
            mutex.acquire();
            PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM auth_token WHERE user_id = ? AND token = ? AND expires_at > ?");
            statement.setString(1, username + DOMAIN);
            statement.setString(2, token);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            return statement.executeQuery().next();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            mutex.release();
        }
    }
    
    private String getRole(String username){
        try {
            mutex.acquire();
            PreparedStatement statement = connection.prepareStatement("SELECT ruolo FROM persona WHERE email = ?");
            statement.setString(1, username + DOMAIN);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return resultSet.getString("ruolo");
            else
                return "";
        } catch (InterruptedException | SQLException ignored) {
            return "";
        } finally {
            mutex.release();
        }
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
    
}
