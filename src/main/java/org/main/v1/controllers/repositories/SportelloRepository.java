package org.main.v1.controllers.repositories;

import org.main.v1.models.Sportello;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SportelloRepository extends JpaRepository<Sportello, Long> {

    // recupera tutti gli sportelli per pagina
    @Query("SELECT s FROM Sportello s")
    Page<Sportello> findAllByPage(Pageable pageable);

    // Recupera tutti gli sportelli con num_iscritti < max_iscritti
    @Query("SELECT DISTINCT s FROM Sportello s " +
            "JOIN Giorno g ON g.sportello = s " +
            "WHERE g.num_iscritti < g.max_iscritti " +
            "AND s.sportello_disponibile = TRUE")
    Page<Sportello> getSportelliDisponibili(Pageable pageable);

    // Recupera gli sportelli per un docente specifico tramite email
    @Query("SELECT s FROM Sportello s WHERE s.docente_responsabile.email = :emailDocente")
    Page<Sportello> getSportelliByDocente(@Param("emailDocente") String emailDocente, Pageable pageable);

    // Recupera tutti gli iscritti di uno sportello
    @Query("SELECT p, i.id.giornoId FROM Persona p " +
            "JOIN IscrizioneSportello i ON i.persona.email = p.email " +
            "WHERE i.sportello.id = :idSportello")
    List<Object[]> getIscrizioniNelloSportello(@Param("idSportello") Long idSportello);

    @Query("SELECT p, i.id.giornoId FROM Persona p " +
            "JOIN IscrizioneSportello i ON i.persona.email = p.email " +
            "WHERE i.sportello.id = :idSportello " +
            "AND i.persona.email = :user")
    List<Object[]> getIscrizioniNelloSportello(@Param("idSportello") Long idSportello, @Param("user") String user);
    
    // Recupera gli sportelli a cui una persona è iscritta
    @Query("SELECT s FROM Sportello s JOIN s.iscrizioni i WHERE i.persona.email = :username")
    Page<Sportello> getSportelliPrenotati(@Param("username") String username, Pageable pageable);

    /* Crea uno sportello
    @Modifying
    @Query("INSERT INTO Sportello (nomeSportello, numIscritti, maxIscritti, aula.id, materia.id, docenteResponsabile) " +
            "VALUES (:nomeSportello, 0, :maxIscritti, :aula, :materia, :docenteResponsabile)")
    int creaSportello(@Param("nomeSportello") String nomeSportello,
                      @Param("maxIscritti") int maxIscritti,
                      @Param("materia") long materia,
                      @Param("aula") long aula,
                      @Param("docenteResponsabile") String docenteResponsabile); */

}
