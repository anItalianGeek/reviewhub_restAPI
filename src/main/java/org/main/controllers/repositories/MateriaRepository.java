package org.main.controllers.repositories;

import org.main.models.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateriaRepository extends JpaRepository<Materia, String> {
}
