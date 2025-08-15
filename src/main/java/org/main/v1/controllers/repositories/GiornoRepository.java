package org.main.v1.controllers.repositories;

import org.main.v1.models.Giorno;
import org.main.v1.models.GiornoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GiornoRepository extends JpaRepository<Giorno, GiornoId> {
    
    // cancella tutti i giorni di uno sportello
    @Query("DELETE FROM Giorno g WHERE g.id.sportelloId = :id")
    @Modifying
    @Transactional
    void cancellaGiorni(@Param("id") long id);

    // aggiorna il numero di iscritti ad ogni subscribe/unsubscribe
    @Query("UPDATE Giorno g SET g.num_iscritti = g.num_iscritti + 1 WHERE g.num_iscritti < g.max_iscritti AND g.id = :giornoId")
    @Modifying
    @Transactional
    int aggiungiIscrittoInternal(@Param("giornoId") GiornoId giornoId);

    @Query("UPDATE Giorno g SET g.num_iscritti = g.num_iscritti - 1 WHERE g.num_iscritti > 0 AND g.id = :giornoId")
    @Modifying
    @Transactional
    int rimuoviIscrittoInternal(@Param("giornoId") GiornoId giornoId);

    default boolean aggiungiIscritto(@Param("giornoId") GiornoId giornoId) {
        return aggiungiIscrittoInternal(giornoId) > 0;
    }

    default boolean rimuoviIscritto(@Param("giornoId") GiornoId giornoId) {
        return rimuoviIscrittoInternal(giornoId) > 0;
    }

}
