package org.example.controllers.repositories;

import org.example.models.Sportello;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SportelloRepository extends JpaRepository<Sportello, Long> {
    
    @Query(value = "SELECT * FROM SportelloDB WHERE num_iscritti < max_iscritti", nativeQuery = true)
    List<Sportello> getSportelliDisponibili();
    
    @Query(value = "SELECT * FROM SportelloDB WHERE docente_responsabile = :email_docente", nativeQuery = true)
    List<Sportello> getSportelliByDocente(@Param("email_docente") String email_docente);
    
    @Query(value = "INSERT INTO iscrizione_sportello (id_sportello, persona_iscritta) VALUES (:id, :username)", nativeQuery = true)
    Sportello iscriviAlloSportello(@Param("id") long id, @Param("username") String username);
    
    @Modifying
    @Query(value = "UPDATE SportelloDB SET num_iscritti = num_iscritti + 1 WHERE id_sportello = :id AND num_iscritti < max_iscritti", nativeQuery = true)
    int aggiungiIscritto(@Param("id") long id);
    
    @Modifying
    @Query(value = "INSERT INTO SportelloDB (nome_sportello, num_iscritti, max_iscritti, aula_id, materia_id, docente_responsabile) VALUES (:nome_sportello, 0, :max_iscritti, :aula, :materia, :docente_responsabile)", nativeQuery = true)
    Sportello creaSportello(@Param("nome_sportello") String nome_sportello, @Param("max_iscritti") int max_iscritti, @Param("materia") long materia, @Param("aula") long aula, @Param("docente-responsabile") String docente_responsabile);

    @Modifying
    @Query(value = "UPDATE SportelloDB SET num_iscritti = num_iscritti - 1 WHERE id_sportello = :id AND num_iscritti > 0", nativeQuery = true)
    int rimuoviIscritto(@Param("id") long id);
    
    @Modifying
    @Query(value = "DELETE FROM iscrizione_sportello WHERE id_sportello = :id AND persona_iscritta = :user")
    int cancellaIscrizione(@Param("id") long id, @Param("user") String user);
    
}
