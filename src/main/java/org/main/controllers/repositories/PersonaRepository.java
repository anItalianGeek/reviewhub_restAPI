package org.main.controllers.repositories;

import org.main.models.Persona;
import org.main.models.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

public interface PersonaRepository extends JpaRepository<Persona, String> {

    // Verifica se una email è disponibile
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Persona p WHERE p.email = :email")
    Boolean verificaMailDisponibile(@Param("email") String email);
    
    /* Aggiungi una nuova persona
    @Transactional
    @Modifying
    @Query("INSERT INTO Persona (email, nome, cognome, password, ruolo, classe) VALUES (:email, :nome, :cognome, :password, :ruolo, :classe)")
    int aggiungiNuovaPersona(@Param("email") String email,
                             @Param("nome") String nome,
                             @Param("cognome") String cognome,
                             @Param("password") String password,
                             @Param("ruolo") UserIdentity ruolo,
                             @Param("classe") String classe);
    */
    
    // Cancella una persona
    @Modifying
    @Transactional
    @Query("DELETE FROM Persona p WHERE p.email = :email")
    int cancellaPersona(@Param("email") String email);
    
    // Verifica se la persona ha un determinato ruolo
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Persona p WHERE p.ruolo = :ruolo AND p.email = :user")
    Boolean verificaRuolo(@Param("ruolo") UserIdentity ruolo, @Param("user") String user);
    
    // Ottieni il ruolo della persona
    @Query("SELECT p.ruolo FROM Persona p WHERE p.email = :user")
    UserIdentity ottieniRuolo(@Param("user") String user);
    
}
