package org.main.essentials;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.main.controllers.socket.ClientStudentHandler;

import javax.net.ssl.SSLSocket;

public final class SocketConnectionManager extends Thread {
    
    private SSLSocket clientConnection;
    private PrintWriter pw;
    private BufferedReader br;
    private Gson gson;
    private ClientStudentHandler studentHandler;
    
    public SocketConnectionManager(SSLSocket clientConnection) {
        try {
            this.clientConnection = clientConnection;
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream())));
            br = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        } catch (IOException ex) {
        }
        gson = new GsonBuilder().create();
    }

    @Override
    public void run() {
        try {
            String clientRequest = br.readLine();
            AccessRequest accessRequest = gson.fromJson(clientRequest, AccessRequest.class);
            if (studentHandler.verificaAutenticita(accessRequest.getUsername(), accessRequest.getPassword(), accessRequest.getHash())) {
            
            } else {
                pw.println("close-connection");
                clientConnection.close();    
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}