package org.example.controllers.repositories;

import org.example.models.Persona;
import org.example.models.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository extends JpaRepository<Persona, String> {
    
    @Modifying
    @Query(value = "INSERT INTO persona (email, nome, cognome, password, ruolo, classe) VALUES (:email, :nome, :cognome, :password, :ruolo, :classe)", nativeQuery = true)
    Persona aggiungiNuovaPersona(@Param("email") String email, @Param("nome") String nome, @Param("cognome") String cognome, @Param("password") String password, @Param("ruolo") UserIdentity ruolo, @Param("classe") String classe);
    
    @Modifying
    @Query(value = "DELETE FROM persona WHERE email = :email")
    int cancellaPersona(@Param("email") String email);
    
}
