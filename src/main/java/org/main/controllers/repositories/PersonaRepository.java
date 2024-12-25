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

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO persona (email, nome, cognome, password, ruolo, classe) VALUES (:email, :nome, :cognome, :password, :ruolo, :classe)", nativeQuery = true)
    int aggiungiNuovaPersona(@Param("email") String email, @Param("nome") String nome, @Param("cognome") String cognome, @Param("password") String password, @Param("ruolo") UserIdentity ruolo, @Param("classe") String classe);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM persona WHERE email = :email", nativeQuery = true)
    int cancellaPersona(@Param("email") String email);
    
    @Query(value = "SELECT TRUE FROM auth_token WHERE token = :provided_token AND user_id = :user AND expires_at > NOW()", nativeQuery = true)
    Boolean verificaCodice(@Param("provided_token") String provided_token, @Param("user") String user);
    
    @Query(value = "SELECT TRUE FROM persona WHERE ruolo = :ruolo AND email = :user", nativeQuery = true)
    Boolean verificaRuolo(@Param("ruolo") UserIdentity ruolo, @Param("user") String user);
    
    @Query(value = "INSERT INTO auth_token (token, user_id) VALUES (:generated_token, :user)", nativeQuery = true)
    @Modifying
    @Transactional
    void aggiungiCodice(@Param("generated_token") String generated_token, @Param("user") String user);
}
