package org.main.controllers.repositories;

import org.main.models.Giorno;
import org.main.models.GiornoId;
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
    
}
