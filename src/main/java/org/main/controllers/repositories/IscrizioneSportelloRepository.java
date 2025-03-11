package org.main.controllers.repositories;

import org.main.models.Giorno;
import org.main.models.GiornoId;
import org.main.models.IscrizioneSportello;
import org.main.models.IscrizioneSportelloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface IscrizioneSportelloRepository extends JpaRepository<IscrizioneSportello, IscrizioneSportelloId> {
    
    /* Iscrivi una persona a uno sportello
    @Modifying
    @Query("INSERT INTO IscrizioneSportello (sportello.id_sportello, persona.email) VALUES (:id, :username)")
    int iscriviAlloSportello(@Param("id") long id, @Param("username") String username); */


    // Cancella un'iscrizione dallo sportello
    @Modifying
    @Transactional
    @Query("DELETE FROM IscrizioneSportello i WHERE i.sportello.id_sportello = :id " +
            "AND i.persona.email = :user " +
            "AND i.giorno.id.data_inizioId = :dataInizio " +
            "AND i.giorno.id.data_fineId = :dataFine")
    int cancellaIscrizione(@Param("id") long id,
                           @Param("user") String user,
                           @Param("dataInizio") LocalDateTime dataInizio,
                           @Param("dataFine") LocalDateTime dataFine);


    // verifica se esiste un'iscrizione
    @Query("SELECT COUNT(i) FROM IscrizioneSportello i WHERE i.sportello.id_sportello = :id AND i.persona.email = :email AND i.giorno.id.data_inizioId = :dataInizio AND i.giorno.id.data_fineId = :dataFine")
    int esisteIscrizione(@Param("id") long id, @Param("email") String email, @Param("dataInizio") LocalDateTime dataInizio, @Param("dataFine") LocalDateTime dataFine);
    
    
    // cancella tutte le iscrizioni dello sportello
    @Query("DELETE FROM IscrizioneSportello i WHERE i.sportello.id_sportello = :id")
    @Modifying
    @Transactional
    void cancellaTutteIscrizioni(@Param("id") long id);
}
