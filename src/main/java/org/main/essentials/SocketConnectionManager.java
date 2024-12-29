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
import org.main.controllers.socket.ClientTeacherHandler;
import org.main.models.Persona;
import org.main.models.Sportello;

import javax.net.ssl.SSLSocket;

public final class SocketConnectionManager extends Thread {
    
    private SSLSocket clientConnection;
    private PrintWriter pw;
    private BufferedReader br;
    private Gson gson;
    private ClientStudentHandler studentHandler;
    private ClientTeacherHandler teacherHandler;
    
    public SocketConnectionManager(SSLSocket clientConnection) {
        try {
            this.clientConnection = clientConnection;
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientConnection.getOutputStream())));
            br = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        } catch (IOException ex) {
        }
        gson = new GsonBuilder().create();
        teacherHandler = null;
        studentHandler = null;
    }

    @Override
    public void run() {
        try {
            String clientRequest = br.readLine();
            AccessRequest accessRequest = gson.fromJson(clientRequest, AccessRequest.class);
            if (studentHandler.verificaAutenticita(accessRequest.getUsername(), accessRequest.getPassword(), accessRequest.getHash())) {
                studentHandler = new ClientStudentHandler();
                pw.println(studentHandler.recuperaToken(accessRequest.getUsername()));
                pw.println(studentHandler.getStudente());
                clientRequest = br.readLine();
                switch (clientRequest) {
                    case "get-sportelli" -> {pw.println(studentHandler.getSportelliDisponibili());}
                    case "get-sportello" -> {
                        String id_sportello = br.readLine();
                        pw.println(studentHandler.getSportelloById(Long.parseLong(id_sportello)));
                    }
                    case "iscrivi-allo-sportello" -> {
                        String id_sportello = br.readLine();
                        pw.println(studentHandler.iscriviAlloSportello(Long.parseLong(id_sportello), accessRequest.getUsername()));
                    }
                    case "disiscrivi-dallo-sportello" -> {
                        String id_sportello = br.readLine();
                        pw.println(studentHandler.disiscriviDalloSportello(Long.parseLong(id_sportello), accessRequest.getUsername()));
                    }
                    case "aggiorna-dati-personali" -> {
                        String datiPersonali = br.readLine();
                        pw.println(studentHandler.aggiornaInformazioniPersonali(gson.fromJson(datiPersonali, Persona.class), accessRequest.getUsername()));
                    }
                    case "sportelli-prenotati" -> {pw.println(studentHandler.getSportelliIscritti(accessRequest.getUsername()));}
                    case "close-connection" -> {clientConnection.close();}
                }
            } else if (teacherHandler.verificaAutenticita(accessRequest.getUsername(), accessRequest.getPassword(), accessRequest.getHash())) {
                teacherHandler = new ClientTeacherHandler();
                pw.println(teacherHandler.recuperaToken(accessRequest.getUsername()));
                pw.println(teacherHandler.getDocente());
                clientRequest = br.readLine();
                switch (clientRequest) {
                    case "get-sportello" -> {
                        String id_sportello = br.readLine();
                        pw.println(studentHandler.getSportelloById(Long.parseLong(id_sportello)));
                    }
                    case "get-sportelli-gestiti" -> {
                        pw.println(teacherHandler.visualizzaSportelliGestiti(accessRequest.getUsername()));
                    }
                    case "crea-sportello" -> {
                        String dati = br.readLine();
                        pw.println(teacherHandler.creaSportello(gson.fromJson(dati, Sportello.class)));
                    }
                    case "modifica-sportello" -> {
                        String dati = br.readLine();
                        pw.println(teacherHandler.modificaSportello(gson.fromJson(dati, Sportello.class)));
                    }
                    case "cancella-sportello" -> {
                        String dati = br.readLine();
                        pw.println(teacherHandler.cancellaSportello(Long.parseLong(dati), accessRequest.getUsername()));
                    }
                    case "aggiorna-dati-personali" -> {
                        String dati = br.readLine();
                        pw.println(teacherHandler.aggiornaInformazioniPersonali(gson.fromJson(dati, Persona.class), accessRequest.getUsername()));
                    } // todo user must log out if email is changed
                    case "close-connection" -> {
                        clientConnection.close();
                    }
                }
                
            } else {
                pw.println("close-connection");
                clientConnection.close();    
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}