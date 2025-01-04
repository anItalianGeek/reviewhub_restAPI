package org.main;

import org.main.essentials.SocketConnectionManager;
import org.main.essentials.Shell;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

@EntityScan(basePackages = "org.main.models")
@EnableJpaRepositories(basePackages = "org.main.controllers.repositories")
@SpringBootApplication
@EnableAsync
public class Main {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
        SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(8889);

        // Start the shell once when the server starts
        new Shell().start();

        // Start accepting connections
        while (true) {
            new SocketConnectionManager((SSLSocket) serverSocket.accept()).start();
        }
    }
    
}
