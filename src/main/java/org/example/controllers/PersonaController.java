package org.example.controllers;

import org.example.controllers.repositories.PersonaRepository;
import org.example.models.Persona;
import org.example.models.UserIdentity;
import org.example.other.SHA256Encryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Id;
import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/users")
public final class PersonaController {
    
    private static final String DOMAIN = "@chilesotti.it";
    
    @Autowired
    private static PersonaRepository personaRepository;
    
    public PersonaController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }
    
    @GetMapping("/all")
    public static List<Persona> getTuttePersone() {
        return personaRepository.findAll();
    }
    
    @GetMapping("/{username}")
    public static Persona getPersonaById(@PathVariable long username) {
        return personaRepository.findById(username + DOMAIN).orElse(null);
    }
    
    @PostMapping("/create")
    public static Persona aggiungiPersona(@RequestBody Persona datiNuovaPersona) {
        return personaRepository.aggiungiNuovaPersona(datiNuovaPersona.getEmail(), datiNuovaPersona.getNome(), datiNuovaPersona.getCognome(), datiNuovaPersona.getPassword(), datiNuovaPersona.getRuolo(), datiNuovaPersona.getClasse());
    }
    
    @PutMapping("/modify/{user}")
    @Transactional
    public static Persona aggiornaPersona(@RequestBody Persona datiPersona, @PathVariable String user) {
        Persona persona = personaRepository.findById(user + DOMAIN).orElse(null);
        
        if (persona == null || !(user + DOMAIN).equals(persona.getEmail()))
            return null;
        else {
            if (!persona.getNome().equals(datiPersona.getNome())) persona.setNome(datiPersona.getNome());
            if (!persona.getCognome().equals(datiPersona.getCognome())) persona.setCognome(datiPersona.getCognome());
            if (!persona.getPassword().equals(datiPersona.getPassword())) persona.setPassword(datiPersona.getPassword());
            if (!persona.getClasse().equals(datiPersona.getClasse())) persona.setClasse(datiPersona.getClasse());
            if (!persona.getSportelli().equals(datiPersona.getSportelli())) persona.setSportelli(datiPersona.getSportelli());
            return personaRepository.save(persona);
        }
    }
    
    @DeleteMapping("/remove/{user}")
    public static void cancellaPersona(@PathVariable String user) {
        int rowsAffected = personaRepository.cancellaPersona(user + DOMAIN);
        // return status code
    }
    
}
