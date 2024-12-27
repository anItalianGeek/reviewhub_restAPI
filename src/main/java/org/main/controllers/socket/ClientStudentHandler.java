package org.main.controllers.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.Semaphore;

public final class ClientStudentHandler {
    
    private static final Semaphore mutex = new Semaphore(1);
    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/reviewhub_db?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true";
    private static final String username = "admin";
    private static final String password = "admin";
    private static Connection connection;
    private static Statement statement;
    private Gson gson;
    
    public ClientStudentHandler() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        gson = new GsonBuilder().create();
    }
    
    public boolean verificaAutenticita(String username, String password, String hash) {
        try {
            ResultSet resultSet = statement.executeQuery("SELECT TRUE FROM persona WHERE email = " + username + " AND password = " + password + " AND ruolo = \"STUDENT\"");
            if (resultSet.next()) {
                resultSet = statement.executeQuery("SELECT TRUE FROM auth_token WHERE token = " + hash + " AND user_id = " + username + " AND expires_at > " + LocalDateTime.now());
                return resultSet.next();
            } else return false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public String getSportelliDisponibili() {
        return null;
    }
    
    public String iscriviAlloSportello() {
        return null;
    }
    
    public String disiscriviDalloSportello() {
        return null;
    }
    
    public String aggiornaInformazioniPersonali() {
        return null;
    }
    
    private void P() {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void V() {
        mutex.release();
    }
    
}
