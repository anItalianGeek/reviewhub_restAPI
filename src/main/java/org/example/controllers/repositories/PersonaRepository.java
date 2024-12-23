package org.example.controllers.repositories;

import org.example.models.dbmodels.PersonaDB;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<PersonaDB, String> {
}
