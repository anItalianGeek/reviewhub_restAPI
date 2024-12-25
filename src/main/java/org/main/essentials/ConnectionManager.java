package org.main.essentials;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConnectionManager extends Thread {
    
    protected Socket clientConnection;
    protected PrintWriter pw;
    protected BufferedReader br;
    protected Gson gson;
    
    public ConnectionManager(Socket clientConnection) {
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
    }
}