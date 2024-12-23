package org.example.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.ServerSocket;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Component
    public static class ServerSocketInitializer {

        @PostConstruct
        public void startServerSocket() throws IOException {
            ServerSocket serverSocket = new ServerSocket(8888);
            // Start the shell once when the server starts
            new Shell().start();

            // Start accepting connections
            while (true) {
                new ConnectionManager(serverSocket.accept()).start();
            }
        }
    }
}
