package org.main.controllers.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mysql.cj.xdevapi.UpdateStatement;
import org.main.controllers.socket.wrappers.Iscrizione;
import org.main.controllers.socket.wrappers.WrapperSportelliDocente;
import org.main.controllers.socket.wrappers.WrapperSportelliStudente;
import org.main.models.*;
import org.main.other.SHA256Encryptor;
import org.main.other.ServerSignatureGenerator;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;

public final class ClientTeacherHandler {

    private static final Semaphore mutex = new Semaphore(1);
    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/reviewhub_db?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true";
    private static final String username = "admin";
    private static final String password = "admin";
    private static Connection connection;
    private static Statement statement;
    private final Gson gson;
    private Persona docente;

    public ClientTeacherHandler() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            statement = connection.createStatement();
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
                    docente = new Persona(
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

    public String getDocente() {
        return gson.toJson(docente);
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
    
    public String visualizzaSportelliGestiti(String username) {
        String query = "SELECT s.*, p.email, p.nome AS docente_nome, p.cognome, m.nome AS materia_nome, a.nome AS aula_nome, g.data_inizio, g.data_fine " +
                    "FROM SportelloDB s " +
                    "JOIN persona p ON s.docente_responsabile = p.email " +
                    "JOIN materia m ON s.materia_id = m.id_materia " +
                    "JOIN aula a ON s.aula_id = a.id " +
                    "JOIN giorno g ON s.id_sportello = g.id_sportello " +
                    "WHERE s.docente_responsabile = ?";
        String queryDos = "SELECT p.nome, p.cognome, p.classe, p.email FROM persona as p" +
                    "JOIN iscrizione_sportello AS i ON i.persona_iscritta = p.email" +
                    "WHERE i.id_sportello = ?";
        try (
            PreparedStatement statement = connection.prepareStatement(query);
            PreparedStatement statementDos = connection.prepareStatement(queryDos);       
        ) {
            P();
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            Map<Long, Sportello> sportelloMap = new HashMap<>();
            LinkedList<Iscrizione> iscritti = new LinkedList<>();
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

                    statementDos.setLong(1, idSportello);
                    ResultSet iscrittiSet = statementDos.executeQuery();
                    Iscrizione iscrittiSportello = new Iscrizione(idSportello, new LinkedList<>());
                    while (iscrittiSet.next())
                        iscrittiSportello.getIscritti().add(iscrittiSet.getString("nome") + " " + iscrittiSet.getString("cognome") + ", " + iscrittiSet.getString("classe") + " (" + iscrittiSet.getString("email") + ")");
                    iscritti.add(iscrittiSportello);
                }
                Giorno giorno = new Giorno(
                        resultSet.getTimestamp("data_inizio").toLocalDateTime(),
                        resultSet.getTimestamp("data_fine").toLocalDateTime(),
                        idSportello
                );
                sportello.getGiorni().add(giorno);
            }
            return gson.toJson(new WrapperSportelliDocente(new LinkedList<>(sportelloMap.values()), iscritti));
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
    
    public String creaSportello(Sportello datiNuovoSportello) {
    String query = "INSERT INTO sportello (nome_sportello, num_iscritti, max_iscritti, docente_responsabile, aula_id, materia_id)" +
                "VALUES (?, 0, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            P();
            statement.setString(1, datiNuovoSportello.getNome_sportello());
            statement.setLong(2, datiNuovoSportello.getMax_iscritti());
            statement.setString(3, datiNuovoSportello.getDocente_responsabile().getEmail());
            statement.setInt(4, datiNuovoSportello.getAula().getId());
            statement.setInt(5, datiNuovoSportello.getMateria().getId());
            
            int righeModificate = statement.executeUpdate();
            if (righeModificate == 0) return "failure";
            else return "success";
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }
    
    public String modificaSportello(Sportello datiNuovoSportello) {
        String query = "SELECT * FROM sportello WHERE docente_responsabile = ? AND id_sportello = ?";
        String updateQuery = "UPDATE sportello SET ? = ? WHERE id_sportello = ?";
        try (
            PreparedStatement statement = connection.prepareStatement(query);
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery)
        ) {
            P();
            statement.setString(1, datiNuovoSportello.getDocente_responsabile().getEmail());
            statement.setLong(2, datiNuovoSportello.getId_sportello());
            updateStatement.setLong(3, datiNuovoSportello.getId_sportello());
            
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (!datiNuovoSportello.getNome_sportello().equals(resultSet.getString("nome_sportello"))) {
                    updateStatement.setString(1, "nome_sportello");
                    updateStatement.setString(2, datiNuovoSportello.getNome_sportello());
                    updateStatement.executeUpdate();
                }
                if (datiNuovoSportello.getMax_iscritti() != resultSet.getInt("max_iscritti")) {
                    updateStatement.setString(1, "max_iscritti");
                    updateStatement.setInt(2, datiNuovoSportello.getMax_iscritti());
                    updateStatement.executeUpdate();
                }
                if (datiNuovoSportello.getAula().getId() != resultSet.getInt("aula_id")) {
                    updateStatement.setString(1, "aula_id");
                    updateStatement.setInt(2, datiNuovoSportello.getAula().getId());
                    updateStatement.executeUpdate();
                }
                if (datiNuovoSportello.getMateria().getId() != resultSet.getInt("materia_id")) {
                    updateStatement.setString(1, "materia_id");
                    updateStatement.setInt(2, datiNuovoSportello.getMateria().getId());
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
    
    public String cancellaSportello(long id, String username) {
        String query = "DELETE FROM sportello WHERE id_sportello = ? AND docente_responsabile = ?"; 
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            P();
            statement.setLong(1, id);
            statement.setString(2, username);
            
            int righeModificate = statement.executeUpdate();
            if (righeModificate == 0) return "failure";
            else return "success";
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }

    public String aggiornaInformazioniPersonali(Persona informazioniPersona, String oldEmail) {
        try (
                PreparedStatement statement = connection.prepareStatement("SELECT nome, cognome, classe, password, email FROM persona WHERE email = ?");
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE persona SET ? = ? WHERE email = ?");
                PreparedStatement searchSportelliStatement = connection.prepareStatement("SELECT id_sportello FROM sportello WHERE docente_responsabile = ?");
                PreparedStatement sportelliUpdateStatement = connection.prepareStatement("UPDATE sportello SET docente_responsabile = ? WHERE id_sportello = ?")
        ) {
            P();
            statement.setString(1, oldEmail);
            searchSportelliStatement.setString(1, oldEmail);
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
                    ResultSet sportelliDaAggiornare = searchSportelliStatement.executeQuery();
                    while (sportelliDaAggiornare.next()) {
                        sportelliUpdateStatement.setString(1, informazioniPersona.getEmail());
                        sportelliUpdateStatement.setLong(2, sportelliDaAggiornare.getLong("id_sportello"));
                        sportelliUpdateStatement.executeUpdate();
                    }
                    
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

