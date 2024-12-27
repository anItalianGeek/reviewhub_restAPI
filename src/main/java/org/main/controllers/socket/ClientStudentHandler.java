package org.main.controllers.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.main.models.*;
import org.springframework.cglib.core.Local;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
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
        try {
            P();
            String query = "SELECT s.*, p.email, p.nome AS docente_nome, p.cognome, m.nome AS materia_nome, a.nome AS aula_nome, g.data_inizio, g.data_fine " +
                    "FROM SportelloDB s " +
                    "JOIN persona p ON s.docente_responsabile = p.email " +
                    "JOIN materia m ON s.materia_id = m.id_materia " +
                    "JOIN aula a ON s.aula_id = a.id " +
                    "JOIN giorno g ON s.id_sportello = g.id_sportello " +
                    "WHERE s.num_iscritti < s.max_iscritti";
            ResultSet resultSet = statement.executeQuery(query);
            Map<Long, Sportello> sportelloMap = new HashMap<>();

            while (resultSet.next()) {
                long idSportello = resultSet.getLong("id_sportello");
                Sportello sportello = sportelloMap.get(idSportello);
                if (sportello == null) {
                    Persona docente = new Persona(
                            resultSet.getString("email"),
                            null,
                            null,
                            UserIdentity.TEACHER,
                            resultSet.getString("docente_nome"),
                            resultSet.getString("cognome"),
                            null
                    );
                    Materia materia = new Materia(resultSet.getString("materia_nome"), Integer.MIN_VALUE, null);
                    Aula aula = new Aula(resultSet.getInt("id"), resultSet.getString("aula_nome"), null);
                    sportello = new Sportello(
                            idSportello,
                            docente,
                            materia,
                            aula,
                            resultSet.getInt("num_iscritti"),
                            resultSet.getInt("max_iscritti"),
                            resultSet.getString("nome_sportello"),
                            new LinkedList<>()
                    );
                    sportelloMap.put(idSportello, sportello);
                }
                Giorno giorno = new Giorno(
                        resultSet.getTimestamp("data_inizio").toLocalDateTime(),
                        resultSet.getTimestamp("data_fine").toLocalDateTime(),
                        idSportello
                );
                sportello.getGiorni().add(giorno);
            }
            return gson.toJson(new WrapperSportelli(new LinkedList<>(sportelloMap.values())));
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
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

class WrapperSportelli {
    private LinkedList<Sportello> sportellos;
    
    public WrapperSportelli(LinkedList<Sportello> list){
        sportellos = list;
    }

    public LinkedList<Sportello> getSportellos() {
        return sportellos;
    }

    public void setSportellos(LinkedList<Sportello> sportellos) {
        this.sportellos = sportellos;
    }
}
