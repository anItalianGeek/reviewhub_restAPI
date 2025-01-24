package org.main.controllers.repositories;

import org.main.models.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaRepository extends JpaRepository<Aula, Integer> {
}
