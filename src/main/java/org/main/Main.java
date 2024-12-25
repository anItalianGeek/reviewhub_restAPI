package org.main;

import org.main.essentials.ConnectionManager;
import org.main.essentials.Shell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.ServerSocket;

@SpringBootApplication
public class Main {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
        ServerSocket serverSocket = new ServerSocket(8889);
        
        // Start the shell once when the server starts
        new Shell().start();

        // Start accepting connections
        while (true) {
            new ConnectionManager(serverSocket.accept()).start();
        }
    }
    
}
