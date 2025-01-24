package org.main.controllers.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.main.models.wrappers.Iscrizione;
import org.main.models.wrappers.WrapperSportelliDocente;
import org.main.models.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.main.controllers.socket.DatabaseContext.connection;
import static org.main.controllers.socket.DatabaseContext.mutex;

public final class ClientTeacherHandler {
    
    private final Gson gson;
    private Persona docente;
    private boolean authenticated;

    public ClientTeacherHandler() {
        gson = new GsonBuilder().create();
        authenticated = false;
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
                            null,
                            null,
                            null
                    );
                    authenticated = true;
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
        if (!authenticated) return null;

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
        if (!authenticated) return null;

        String query = "SELECT s.*, p.email, p.nome AS docente_nome, p.cognome, m.nome AS materia_nome, a.nome AS aula_nome, g.data_inizio, g.data_fine " +
                "FROM SportelloDB s " +
                "JOIN persona p ON s.docente_responsabile = p.email " +
                "JOIN materia m ON s.materia_id = m.nome " +
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
                            null,
                            null,
                            null
                    );
                    Materia materia = new Materia(resultSet.getString("materia_nome"), null);
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
                            new LinkedList<>(),
                            null
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
        if (!authenticated) return null;

        String query = "SELECT s.*, p.email, p.nome AS docente_nome, p.cognome, m.nome AS materia_nome, a.nome AS aula_nome, g.data_inizio, g.data_fine " +
                "FROM SportelloDB s " +
                "JOIN persona p ON s.docente_responsabile = p.email " +
                "JOIN materia m ON s.materia_id = m.nome " +
                "JOIN aula a ON s.aula_id = a.id " +
                "JOIN giorno g ON s.id_sportello = g.id_sportello " +
                "JOIN iscrizione_sportello i ON s.id_sportello = i.id_sportello " +
                "WHERE s.id_sportello = ?"; // Usando parametro
        String queryDos = "SELECT p.nome, p.cognome, p.classe, p.email FROM persona as p" +
                "JOIN iscrizione_sportello AS i ON i.persona_iscritta = p.email" +
                "WHERE i.id_sportello = ?";
        try (
                PreparedStatement selectStatement = connection.prepareStatement(query);
                PreparedStatement selectIscrittiStatement = connection.prepareStatement(queryDos);
        ) {
            P();
            selectStatement.setLong(1, id); // Imposta l'id come parametro
            ResultSet resultSet = selectStatement.executeQuery();

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
                            null,
                            null,
                            null
                    );
                    Materia materia = new Materia(resultSet.getString("materia_nome"), null);
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
                            new LinkedList<>(),
                            null
                    );
                    sportelloMap.put(idSportello, sportello);

                    selectIscrittiStatement.setLong(1, idSportello);
                    ResultSet iscrittiSet = selectIscrittiStatement.executeQuery();
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

    public String creaSportello(Sportello datiNuovoSportello) {
        if (!authenticated) return "failure";

        String querySportello = "INSERT INTO sportello (nome_sportello, num_iscritti, max_iscritti, docente_responsabile, aula_id, materia_id)" +
                " VALUES (?, 0, ?, ?, ?, ?)";
        String queryGiorni = "INSERT INTO giorno VALUES (?, ?, ?)";

        try (
                PreparedStatement sportelloStatement = connection.prepareStatement(querySportello, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement giorniStatement = connection.prepareStatement(queryGiorni)
        ) {
            P();
            // Imposta i parametri della query
            sportelloStatement.setString(1, datiNuovoSportello.getNome_sportello());
            sportelloStatement.setLong(2, datiNuovoSportello.getMax_iscritti());
            sportelloStatement.setString(3, datiNuovoSportello.getDocente_responsabile().getEmail());
            sportelloStatement.setInt(4, datiNuovoSportello.getAula().getId());
            sportelloStatement.setString(5, datiNuovoSportello.getMateria().getNome());

            int righeModificate = sportelloStatement.executeUpdate();

            if (righeModificate == 0) {
                return "failure";
            }

            // Recupera l'ID generato
            try (ResultSet generatedKeys = sportelloStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idSportello = generatedKeys.getLong(1);
                    datiNuovoSportello.setId_sportello(idSportello); // Imposta l'ID nello sportello

                    // Inserisci i giorni associati
                    for (Giorno giorno : datiNuovoSportello.getGiorni()) {
                        giorniStatement.setTimestamp(2, Timestamp.valueOf(giorno.getId().getData_inizioId()));
                        giorniStatement.setTimestamp(1, Timestamp.valueOf(giorno.getId().getData_fineId()));
                        giorniStatement.setLong(3, idSportello);
                        giorniStatement.executeUpdate();
                    }
                    connection.commit();
                    return "success";
                } else {
                    connection.rollback();
                    return "failure";
                }
            }
        } catch (SQLException se) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return "failure";
        } finally {
            V();
        }
    }

    public String modificaSportello(Sportello datiNuovoSportello) {
        if (!authenticated) return "failure";

        String query = "SELECT * FROM sportello WHERE docente_responsabile = ? AND id_sportello = ?";
        String updateQueryTemplate = "UPDATE sportello SET %s WHERE id_sportello = ?";
        String recuperaGiorniQuery = "SELECT * FROM giorno WHERE id_sportello = ?";
        String cancellaVecchiGiorniQuery = "DELETE FROM giorno WHERE id_sportello = ?";
        String inserisciNuoviGiorni = "INSERT INTO giorno VALUES (?, ?, ?)";

        try (
                PreparedStatement statement = connection.prepareStatement(query);
                PreparedStatement recuperaGiorni = connection.prepareStatement(recuperaGiorniQuery);
                PreparedStatement cancellaGiorniStatement = connection.prepareStatement(cancellaVecchiGiorniQuery);
                PreparedStatement inserisciGiorniStatement = connection.prepareStatement(inserisciNuoviGiorni)
        ) {
            P();
            statement.setString(1, datiNuovoSportello.getDocente_responsabile().getEmail());
            statement.setLong(2, datiNuovoSportello.getId_sportello());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                long idSportello = resultSet.getLong("id_sportello");

                // StringBuilder per aggiornamenti dinamici
                StringBuilder updateQuery = new StringBuilder();
                List<Object> params = new ArrayList<>();

                // Controllo e costruzione della query per 'nome_sportello'
                if (!datiNuovoSportello.getNome_sportello().equals(resultSet.getString("nome_sportello"))) {
                    updateQuery.append("nome_sportello = ?, ");
                    params.add(datiNuovoSportello.getNome_sportello());
                }

                // Controllo e costruzione della query per 'aula_id'
                if (datiNuovoSportello.getAula().getId() != resultSet.getInt("aula_id")) {
                    updateQuery.append("aula_id = ?, ");
                    params.add(datiNuovoSportello.getAula().getId());
                }

                // Controllo e costruzione della query per 'materia_id'
                if (!datiNuovoSportello.getMateria().equals(resultSet.getString("materia_id"))) {
                    updateQuery.append("materia_id = ?, ");
                    params.add(datiNuovoSportello.getMateria().getNome());
                }

                // Controllo e costruzione della query per 'max_iscritti'
                if (datiNuovoSportello.getMax_iscritti() != resultSet.getInt("max_iscritti")) {
                    if (datiNuovoSportello.getMax_iscritti() < resultSet.getInt("num_iscritti"))
                        return "failure"; // non è possibile ridurre il numero massimo di iscritti
                    updateQuery.append("max_iscritti = ?, ");
                    params.add(datiNuovoSportello.getMax_iscritti());
                }

                // Se ci sono stati aggiornamenti, esegui la query
                if (updateQuery.length() > 0) {
                    // Rimuovi l'ultima virgola e spazio
                    updateQuery.setLength(updateQuery.length() - 2);
                    updateQuery.append(" WHERE id_sportello = ?");
                    params.add(idSportello);

                    // Crea un nuovo PreparedStatement per ogni aggiornamento
                    PreparedStatement newUpdateStatement = connection.prepareStatement(String.format(updateQueryTemplate, updateQuery.toString()));
                    for (int i = 0; i < params.size(); i++) {
                        newUpdateStatement.setObject(i + 1, params.get(i));
                    }
                    newUpdateStatement.executeUpdate();
                }

                // Recupero e ordinamento dei giorni
                recuperaGiorni.setLong(1, idSportello);
                resultSet = recuperaGiorni.executeQuery();
                LinkedList<Giorno> giorniVecchi = new LinkedList<>();
                while (resultSet.next()) {
                    Giorno giorno = new Giorno(
                            resultSet.getTimestamp("data_inizio").toLocalDateTime(),
                            resultSet.getTimestamp("data_fine").toLocalDateTime(),
                            idSportello
                    );
                    giorniVecchi.add(giorno);
                }
                giorniVecchi.sort((a, b) -> a.getId().getData_inizioId().compareTo(b.getId().getData_inizioId()));
                datiNuovoSportello.getGiorni().sort((a, b) -> a.getId().getData_inizioId().compareTo(b.getId().getData_inizioId()));

                // Se i giorni sono diversi, cancella e reinserisci
                if (!giorniVecchi.equals(datiNuovoSportello.getGiorni())) {
                    cancellaGiorniStatement.setLong(1, idSportello);
                    cancellaGiorniStatement.executeUpdate();

                    inserisciGiorniStatement.setLong(3, idSportello);
                    for (Giorno giorno : datiNuovoSportello.getGiorni()) {
                        inserisciGiorniStatement.setTimestamp(1, Timestamp.valueOf(giorno.getId().getData_fineId()));
                        inserisciGiorniStatement.setTimestamp(2, Timestamp.valueOf(giorno.getId().getData_inizioId()));
                        inserisciGiorniStatement.executeUpdate();
                    }
                }

                // Commit della transazione
                connection.commit();
                return "success";
            } else {
                connection.rollback();
                return "failure";
            }
        } catch (SQLException se) {
            try {
                // Rollback in caso di errore
                connection.rollback();
            } catch (SQLException e) {
                throw new RuntimeException("Errore durante il rollback", e);
            }
            throw new RuntimeException("Errore durante la modifica dello sportello", se);
        } finally {
            V();
        }
    }


    public String cancellaSportello(long id, String username) {
        if (!authenticated) return "failure";

        String query = "DELETE FROM sportello WHERE id_sportello = ? AND docente_responsabile = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            P();
            statement.setLong(1, id);
            statement.setString(2, username);

            int righeModificate = statement.executeUpdate();
            if (righeModificate == 0) {
                connection.rollback();
                return "failure";
            } else {
                connection.commit();
                return "success";
            }
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }

    public String aggiornaInformazioniPersonali(Persona informazioniPersona, String oldEmail) {
        if (!authenticated) return "failure";

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

                connection.commit();
                return "success";
            } else {
                connection.rollback();
                return "failure";
            }
        } catch (SQLException se) {
            throw new RuntimeException(se);
        } finally {
            V();
        }
    }

    private void P() {
        try {
            mutex.acquire();
            connection.setAutoCommit(false);
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void V() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        mutex.release();
    }


}

