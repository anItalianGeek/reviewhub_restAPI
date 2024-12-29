package org.main.controllers.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.main.controllers.socket.wrappers.WrapperSportelliStudente;
import org.main.models.*;
import org.main.other.SHA256Encryptor;
import org.main.other.ServerSignatureGenerator;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;

public final class ClientStudentHandler {
    
    private static final Semaphore mutex = new Semaphore(1);
    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/reviewhub_db?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true";
    private static final String username = "admin";
    private static final String password = "admin";
    private static Connection connection;
    private final Gson gson;
    private Persona studente;
    
    public ClientStudentHandler() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        gson = new GsonBuilder().create();
    }
    
    public boolean verificaAutenticita(String username, String password, String hash) {
        String query = "SELECT * FROM persona WHERE email = ? AND password = ? AND ruolo = \"STUDENT\"";
        String queryDos = "SELECT TRUE FROM auth_token WHERE token = ? AND user_id = ? AND expires_at > " + LocalDateTime.now();
        try (
            PreparedStatement statement = connection.prepareStatement(query);
            PreparedStatement statementDos = connection.prepareStatement(queryDos);
        ) {
            P();
            statement.setString(1, username);
            statement.setString(2, password);
            statementDos.setString(1, hash);
            statementDos.setString(2, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ResultSet resultSetDos = statementDos.executeQuery();
                if (resultSetDos.next()) {
                    studente = new Persona(
                            resultSet.getString("email"),
                            resultSet.getString("classe"),
                            resultSet.getString("password"),
                            UserIdentity.STUDENT,
                            resultSet.getString("cognome"),
                            resultSet.getString("nome"),
                            null
                    );
                    return true;
                } else return false;
            } else return false;
        } catch (SQLException e) {
            return false;
        } finally {
            V();
        }
    }

    public String getStudente() {
        return gson.toJson(studente);
    }

    public String recuperaToken(String username) {
        String query = "SELECT token FROM auth_token WHERE user_id = ? AND expires_at > " + LocalDateTime.now();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return resultSet.getString("token");
            else return null;
        } catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }
    
    public String getSportelliDisponibili() {
        try (Statement statement = connection.createStatement()) {
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
                            resultSet.getString("nome_sportello"),
                            resultSet.getString("descrizione_sportello"),
                            docente,
                            materia,
                            aula,
                            resultSet.getInt("num_iscritti"),
                            resultSet.getInt("max_iscritti"),
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
            return gson.toJson(new WrapperSportelliStudente(new LinkedList<>(sportelloMap.values())));
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }

    public String getSportelliIscritti(String username) {
        try (Statement statement = connection.createStatement()) {
            P();
            String query = "SELECT s.*, p.email, p.nome AS docente_nome, p.cognome, m.nome AS materia_nome, a.nome AS aula_nome, g.data_inizio, g.data_fine " +
                    "FROM SportelloDB s " +
                    "JOIN persona p ON s.docente_responsabile = p.email " +
                    "JOIN materia m ON s.materia_id = m.id_materia " +
                    "JOIN aula a ON s.aula_id = a.id " +
                    "JOIN giorno g ON s.id_sportello = g.id_sportello " +
                    "JOIN iscrizione_sportello i ON s.id_sportello = i.id_sportello " +
                    "WHERE i.persona_iscritta = ?"; // Usando parametro
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username); // Imposta l'username come parametro
            ResultSet resultSet = preparedStatement.executeQuery();

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
                            resultSet.getString("nome_sportello"),
                            resultSet.getString("descrizione_sportello"),
                            docente,
                            materia,
                            aula,
                            resultSet.getInt("num_iscritti"),
                            resultSet.getInt("max_iscritti"),
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
            return gson.toJson(new WrapperSportelliStudente(new LinkedList<>(sportelloMap.values())));
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }

    public String getSportelloById(long id) {
        try (Statement statement = connection.createStatement()) {
            P();
            String query = "SELECT s.*, p.email, p.nome AS docente_nome, p.cognome, m.nome AS materia_nome, a.nome AS aula_nome, g.data_inizio, g.data_fine " +
                    "FROM SportelloDB s " +
                    "JOIN persona p ON s.docente_responsabile = p.email " +
                    "JOIN materia m ON s.materia_id = m.id_materia " +
                    "JOIN aula a ON s.aula_id = a.id " +
                    "JOIN giorno g ON s.id_sportello = g.id_sportello " +
                    "JOIN iscrizione_sportello i ON s.id_sportello = i.id_sportello " +
                    "WHERE s.id_sportello = ?"; // Usando parametro
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id); // Imposta l'id come parametro
            ResultSet resultSet = preparedStatement.executeQuery();

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
                            resultSet.getString("nome_sportello"),
                            resultSet.getString("descrizione_sportello"),
                            docente,
                            materia,
                            aula,
                            resultSet.getInt("num_iscritti"),
                            resultSet.getInt("max_iscritti"),
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
            return gson.toJson(new WrapperSportelliStudente(new LinkedList<>(sportelloMap.values())));
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }

    public String iscriviAlloSportello(long id_sportello, String persona_iscritta) {
        String updateQuery = "UPDATE sportello SET num_iscritti = num_iscritti + 1 WHERE id_sportello = ? AND num_iscritti < max_iscritti";
        String insertQuery = "INSERT INTO iscrizione_sportello (persona_iscritta, id_sportello) VALUES (?, ?)";
        try (
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery)
        ) {
            updateStatement.setLong(1, id_sportello);
            P();
            int rightAggiornate = updateStatement.executeUpdate();
            if (rightAggiornate == 0) {
                return "failure";
            } else {
                insertStatement.setString(1, persona_iscritta);
                insertStatement.setLong(2, id_sportello);
                rightAggiornate = insertStatement.executeUpdate();
                
                if (rightAggiornate == 0) {
                    PreparedStatement resetStatement = connection.prepareStatement("UPDATE sportello SET num_iscritti = num_iscritti - 1 WHERE id_sportello = ? AND num_iscritti < max_iscritti");
                    resetStatement.setLong(1, id_sportello);
                    resetStatement.executeUpdate();
                    return "failure";
                } else return "success";
            }
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }
    
    public String disiscriviDalloSportello(long id_sportello, String persona_iscritta) {
        String updateQuery = "UPDATE sportello SET num_iscritti = num_iscritti - 1 WHERE id_sportello = ?";
        String deleteQuery = "DELETE FROM iscrizione_sportello WHERE id_sportello = ? AND persona_iscritta = ?";
        try (
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                PreparedStatement insertStatement = connection.prepareStatement(deleteQuery)
        ) {
            updateStatement.setLong(1, id_sportello);
            P();
            int rightAggiornate = updateStatement.executeUpdate();
            if (rightAggiornate == 0) {
                return "failure";
            } else {
                insertStatement.setString(1, persona_iscritta);
                insertStatement.setLong(2, id_sportello);
                rightAggiornate = insertStatement.executeUpdate();

                if (rightAggiornate == 0) {
                    PreparedStatement resetStatement = connection.prepareStatement("UPDATE sportello SET num_iscritti = num_iscritti + 1 WHERE id_sportello = ? AND num_iscritti < max_iscritti");
                    resetStatement.setLong(1, id_sportello);
                    resetStatement.executeUpdate();
                    return "failure";
                } else return "success";
            }
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }
    
    public String aggiornaInformazioniPersonali(Persona informazioniPersona, String oldEmail) {
        try (
            PreparedStatement statement = connection.prepareStatement("SELECT nome, cognome, classe, password, email FROM persona WHERE email = ?");
            PreparedStatement updateStatement = connection.prepareStatement("UPDATE persona SET ? = ? WHERE email = ?")
        ) {
            P();
            statement.setString(1, oldEmail);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                updateStatement.setString(3, resultSet.getString("email"));

                if (!informazioniPersona.getNome().equals(resultSet.getString("nome"))) {
                    updateStatement.setString(1, "nome");
                    updateStatement.setString(2, informazioniPersona.getNome());
                    updateStatement.executeUpdate();
                }
                if (!informazioniPersona.getCognome().equals(resultSet.getString("cognome"))) {
                    updateStatement.setString(1, "cognome");
                    updateStatement.setString(2, informazioniPersona.getCognome());
                    updateStatement.executeUpdate();
                }
                if (!informazioniPersona.getClasse().equals(resultSet.getString("classe"))) {
                    updateStatement.setString(1, "classe");
                    updateStatement.setString(2, informazioniPersona.getClasse());
                    updateStatement.executeUpdate();
                }
                if (!informazioniPersona.getPassword().equals(resultSet.getString("password"))) {
                    updateStatement.setString(1, "password");
                    updateStatement.setString(2, informazioniPersona.getPassword());
                    updateStatement.executeUpdate();
                }
                if (!informazioniPersona.getEmail().equals(resultSet.getString("email"))) {
                    updateStatement.setString(1, "email");
                    updateStatement.setString(2, informazioniPersona.getEmail());
                    updateStatement.executeUpdate();
                }
                
                return "success";
            } else return "failure";
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
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

