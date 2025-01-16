package org.main.controllers.repositories;

import org.main.models.AuthToken;
import org.main.models.AuthTokenId;
import org.main.models.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, AuthTokenId> {

    // Verifica se un token esiste e non è scaduto
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END " +
            "FROM AuthToken t " +
            "WHERE t.id.tokenId = :provided_token " +
            "AND t.user.email = :user " +
            "AND t.id.expiresAtId > CURRENT_TIMESTAMP")
    Boolean verificaCodice(@Param("provided_token") String providedToken,
                           @Param("user") String userEmail);


    // Verifica se il token è scaduto
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM AuthToken a " +
            "WHERE a.id.tokenId = :providedToken " +
            "AND a.id.userId = :username " +
            "AND a.id.expiresAtId < CURRENT_TIMESTAMP")
    boolean verificaCodiceScaduto(@Param("providedToken") String providedToken, @Param("username") String username);

    // Ottieni il ruolo dell'utente
    @Query("SELECT p.ruolo FROM Persona p WHERE p.email = :user")
    UserIdentity ottieniRuolo(@Param("user") String user);
    
}
