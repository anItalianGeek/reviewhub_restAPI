package org.main.v1.controllers.repositories;

import org.springframework.data.domain.Page;
import org.main.v1.models.Aula;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AulaRepository extends JpaRepository<Aula, Integer> {

    @Query("SELECT a FROM Aula a")
    Page<Aula> findAllByPage(Pageable pageable);

}
