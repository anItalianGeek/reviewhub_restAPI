package org.example.controllers.repositories;

import org.example.models.dbmodels.SportelloDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.lang.model.element.Name;
import java.util.List;

public interface SportelloRepository extends JpaRepository<SportelloDB, Long> {
    
    @Query("SELECT s FROM SportelloDB s WHERE s.num_iscritti < s.max_iscritti")
    List<SportelloDB> getSportelliDisponibili();
    
    @Query("SELECT s FROM SportelloDB s WHERE s.docente_responsabile.email = :email_docente")
    List<SportelloDB> getSportelliByDocente(@Param("email_docente") String email_docente);
    
}
