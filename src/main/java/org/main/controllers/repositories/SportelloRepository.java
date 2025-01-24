package org.main.controllers.repositories;

import org.main.models.Persona;
import org.main.models.Sportello;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SportelloRepository extends JpaRepository<Sportello, Long> {

    // Recupera tutti gli sportelli con num_iscritti < max_iscritti
    @Query("SELECT s FROM Sportello s WHERE s.num_iscritti < s.max_iscritti")
    List<Sportello> getSportelliDisponibili();

    // Recupera gli sportelli per un docente specifico tramite email
    @Query("SELECT s FROM Sportello s WHERE s.docente_responsabile.email = :emailDocente")
    List<Sportello> getSportelliByDocente(@Param("emailDocente") String emailDocente);

    // Recupera tutti gli iscritti di uno sportello
    @Query("SELECT p " +
            "FROM Persona p " +
            "JOIN IscrizioneSportello i ON i.persona.email = p.email " +
            "WHERE i.sportello.id = :idSportello")
    List<Persona> getIscrittiNelloSportello(@Param("idSportello") Long idSportello);

    // Recupera gli sportelli a cui una persona è iscritta
    @Query("SELECT s FROM Sportello s JOIN s.iscrizioni i WHERE i.persona.email = :username")
    List<Sportello> getSportelliPrenotati(@Param("username") String username);

    // Aggiungi un iscritto a uno sportello
    @Modifying
    @Transactional
    @Query("UPDATE Sportello s SET s.num_iscritti = s.num_iscritti + 1 WHERE s.id = :id AND s.num_iscritti < s.max_iscritti")
    int aggiungiIscritto(@Param("id") long id);

    /* Crea uno sportello
    @Modifying
    @Query("INSERT INTO Sportello (nomeSportello, numIscritti, maxIscritti, aula.id, materia.id, docenteResponsabile) " +
            "VALUES (:nomeSportello, 0, :maxIscritti, :aula, :materia, :docenteResponsabile)")
    int creaSportello(@Param("nomeSportello") String nomeSportello,
                      @Param("maxIscritti") int maxIscritti,
                      @Param("materia") long materia,
                      @Param("aula") long aula,
                      @Param("docenteResponsabile") String docenteResponsabile); */

    // Rimuovi un iscritto dallo sportello
    @Modifying
    @Transactional
    @Query("UPDATE Sportello s SET s.num_iscritti = s.num_iscritti - 1 WHERE s.id_sportello = :id AND s.num_iscritti > 0")
    int rimuoviIscritto(@Param("id") long id);

}
