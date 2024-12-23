package org.example.controllers;

import org.example.controllers.repositories.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class PersonaController {
    
    @Autowired
    private PersonaRepository personaRepository;
    
    
    
}
