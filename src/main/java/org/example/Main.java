package org.example;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        
        while (true)
            new ConnectionManager(serverSocket.accept()).start();
    }
}