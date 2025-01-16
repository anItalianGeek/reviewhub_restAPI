package org.main.controllers.repositories;

import org.main.models.IscrizioneSportello;
import org.main.models.IscrizioneSportelloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IscrizioneSportelloRepository extends JpaRepository<IscrizioneSportello, IscrizioneSportelloId> {
    
    /* Iscrivi una persona a uno sportello
    @Transactional
    @Modifying
    @Query("INSERT INTO IscrizioneSportello (sportello.id_sportello, persona.email) VALUES (:id, :username)")
    int iscriviAlloSportello(@Param("id") long id, @Param("username") String username); */


    // Cancella un'iscrizione dallo sportello
    @Modifying
    @Query("DELETE FROM IscrizioneSportello i WHERE i.sportello.id_sportello = :id AND i.persona.email = :user")
    int cancellaIscrizione(@Param("id") long id, @Param("user") String user);
    
}
