package org.main.v1.controllers.repositories;

import org.main.v1.models.Materia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MateriaRepository extends JpaRepository<Materia, String> {

    @Query("SELECT m FROM Materia m")
    Page<Materia> findAllByPage(Pageable pageable);

}
