package org.main.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.main.controllers.repositories.PersonaRepository;
import org.main.models.Persona;
import org.main.models.UserIdentity;
import org.main.other.SHA256Encryptor;
import org.main.other.ServerSignatureGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class PersonaController {
    
    private static final String DOMAIN = "@chilesotti.it";
    private PersonaRepository personaRepository;

    @Autowired
    public PersonaController(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Persona>> getTuttePersone() {
        return new ResponseEntity<>(personaRepository.findAll(), HttpStatus.OK);
    }
    
    @GetMapping("/{username}")
    public ResponseEntity<Persona> getPersonaById(@PathVariable String username) {
        return new ResponseEntity<>(personaRepository.findById(username + DOMAIN).orElse(null), HttpStatus.OK);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> accedi(@RequestBody Persona persona, HttpServletRequest request) {
        String token = SHA256Encryptor.encrypt(ServerSignatureGenerator.generateSignature());
        personaRepository.aggiungiCodice(token, persona.getEmail());
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }
    
    @PostMapping("/create") // todo users must be able to create an account without needing the admin to approve
    public ResponseEntity<String> aggiungiPersona(@RequestBody Persona datiNuovaPersona) {
        int operationResult = personaRepository.aggiungiNuovaPersona(datiNuovaPersona.getEmail(), datiNuovaPersona.getNome(), datiNuovaPersona.getCognome(), datiNuovaPersona.getPassword(), datiNuovaPersona.getRuolo(), datiNuovaPersona.getClasse());
        if (operationResult == 0)
            return new ResponseEntity<>("Operazione fallita.", HttpStatus.INTERNAL_SERVER_ERROR);
        else
            return new ResponseEntity<>("Operazione compiuta con successo.", HttpStatus.CREATED);
    }
    
    @PutMapping("/modify/{user}")
    @Transactional
    public ResponseEntity<Persona> aggiornaPersona(@RequestBody Persona datiPersona, @PathVariable String user) {
        Persona persona = personaRepository.findById(user + DOMAIN).orElse(null);
        
        if (persona == null || !(user + DOMAIN).equals(persona.getEmail()))
            return null;
        else {
            if (!persona.getNome().equals(datiPersona.getNome())) persona.setNome(datiPersona.getNome());
            if (!persona.getCognome().equals(datiPersona.getCognome())) persona.setCognome(datiPersona.getCognome());
            if (!persona.getPassword().equals(datiPersona.getPassword())) persona.setPassword(datiPersona.getPassword());
            if (!persona.getClasse().equals(datiPersona.getClasse())) persona.setClasse(datiPersona.getClasse());
            if (!persona.getSportelli().equals(datiPersona.getSportelli())) persona.setSportelli(datiPersona.getSportelli());
            return new ResponseEntity<>(personaRepository.save(persona), HttpStatus.ACCEPTED);
        }
    }
    
    @DeleteMapping("/remove/{user}")
    public ResponseEntity<String> cancellaPersona(@PathVariable String user, @RequestParam String author, HttpServletRequest request) {
        int rowsAffected = personaRepository.cancellaPersona(user + DOMAIN);
        if (rowsAffected == 0)
            return new ResponseEntity<>("Operazione fallita o senza effetto.", HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>("Operazione compiuta.", HttpStatus.NO_CONTENT);
    }
    
}
