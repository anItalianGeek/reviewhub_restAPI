package org.main.controllers.repositories;

import org.main.models.Sportello;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SportelloRepository extends JpaRepository<Sportello, Long> {

    // Recupera tutti gli sportelli con num_iscritti < max_iscritti
    @Query("SELECT s FROM Sportello s WHERE s.numIscritti < s.maxIscritti")
    List<Sportello> getSportelliDisponibili();

    // Recupera gli sportelli per un docente specifico tramite email
    @Query("SELECT s FROM Sportello s WHERE s.docenteResponsabile = :emailDocente")
    List<Sportello> getSportelliByDocente(@Param("emailDocente") String emailDocente);

    // Recupera gli sportelli a cui una persona è iscritta
    @Query("SELECT s FROM Sportello s JOIN s.iscrizioni i WHERE i.persona.email = :username")
    List<Sportello> getSportelliPrenotati(@Param("username") String username);

    // Iscrivi una persona a uno sportello
    @Transactional
    @Modifying
    @Query("INSERT INTO IscrizioneSportello (sportello.id, persona.email) VALUES (:id, :username)")
    int iscriviAlloSportello(@Param("id") long id, @Param("username") String username);

    // Aggiungi un iscritto a uno sportello
    @Transactional
    @Modifying
    @Query("UPDATE Sportello s SET s.numIscritti = s.numIscritti + 1 WHERE s.id = :id AND s.numIscritti < s.maxIscritti")
    int aggiungiIscritto(@Param("id") long id);

    // Crea uno sportello
    @Transactional
    @Modifying
    @Query("INSERT INTO Sportello (nomeSportello, numIscritti, maxIscritti, aula.id, materia.id, docenteResponsabile) " +
            "VALUES (:nomeSportello, 0, :maxIscritti, :aula, :materia, :docenteResponsabile)")
    int creaSportello(@Param("nomeSportello") String nomeSportello,
                      @Param("maxIscritti") int maxIscritti,
                      @Param("materia") long materia,
                      @Param("aula") long aula,
                      @Param("docenteResponsabile") String docenteResponsabile);

    // Rimuovi un iscritto dallo sportello
    @Transactional
    @Modifying
    @Query("UPDATE Sportello s SET s.numIscritti = s.numIscritti - 1 WHERE s.id = :id AND s.numIscritti > 0")
    int rimuoviIscritto(@Param("id") long id);

    // Cancella un'iscrizione dallo sportello
    @Transactional
    @Modifying
    @Query("DELETE FROM IscrizioneSportello i WHERE i.sportello.id = :id AND i.persona.email = :user")
    int cancellaIscrizione(@Param("id") long id, @Param("user") String user);
}
