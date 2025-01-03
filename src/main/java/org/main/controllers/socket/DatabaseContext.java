package org.main.controllers.socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

public class DatabaseContext {

    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/reviewhub_db?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true";
    private static final String username = "admin";
    private static final String password = "admin";
    public static final Connection connection;
    public static final Semaphore mutex = new Semaphore(1);
    
    static {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
}
