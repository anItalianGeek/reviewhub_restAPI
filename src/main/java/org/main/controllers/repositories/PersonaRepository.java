package org.main.controllers.repositories;

import org.main.models.Persona;
import org.main.models.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, String> {

    // Aggiungi una nuova persona
    @Transactional
    @Modifying
    @Query("INSERT INTO Persona (email, nome, cognome, password, ruolo, classe) VALUES (:email, :nome, :cognome, :password, :ruolo, :classe)")
    int aggiungiNuovaPersona(@Param("email") String email,
                             @Param("nome") String nome,
                             @Param("cognome") String cognome,
                             @Param("password") String password,
                             @Param("ruolo") UserIdentity ruolo,
                             @Param("classe") String classe);

    // Cancella una persona
    @Transactional
    @Modifying
    @Query("DELETE FROM Persona p WHERE p.email = :email")
    int cancellaPersona(@Param("email") String email);

    // Verifica se un token esiste e non è scaduto
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END FROM AuthToken t WHERE t.token = :provided_token AND t.user.id = :user AND t.expiresAt > CURRENT_TIMESTAMP")
    Boolean verificaCodice(@Param("provided_token") String provided_token, @Param("user") String user);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM AuthToken a " +
            "WHERE a.token = :providedToken " +
            "AND a.userId = :username " +
            "AND a.expiresAt < CURRENT_TIMESTAMP")
    boolean verificaCodiceScaduto(@Param("providedToken") String providedToken, @Param("username") String username);
    
    // Verifica se la persona ha un determinato ruolo
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Persona p WHERE p.ruolo = :ruolo AND p.email = :user")
    Boolean verificaRuolo(@Param("ruolo") UserIdentity ruolo, @Param("user") String user);

    // Ottieni il ruolo della persona
    @Query("SELECT p.ruolo FROM Persona p WHERE p.email = :user")
    UserIdentity ottieniRuolo(@Param("user") String user);

    // Aggiungi un token di autenticazione
    @Transactional
    @Modifying
    @Query("INSERT INTO AuthToken (token, user) VALUES (:generated_token, :user)")
    void aggiungiCodice(@Param("generated_token") String generated_token, @Param("user") String user);
}
