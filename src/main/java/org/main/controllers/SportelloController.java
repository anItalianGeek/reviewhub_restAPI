package org.main.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.main.controllers.repositories.PersonaRepository;
import org.main.controllers.repositories.SportelloRepository;
import org.main.models.Persona;
import org.main.models.Sportello;
import org.main.models.UserIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sportello")
public class SportelloController {

    private static final String DOMAIN = "@chilesotti.it";
    private final SportelloRepository sportelloRepository;
    private final PersonaRepository personaRepository;
    
    @Autowired
    public SportelloController(SportelloRepository sportelloRepository, PersonaRepository personaRepository){
        this.sportelloRepository = sportelloRepository;
        this.personaRepository = personaRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Sportello>> getSportelli(@RequestParam String author, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), author + DOMAIN);
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            else {
                permissionCheck = personaRepository.verificaRuolo(UserIdentity.ADMIN, author + DOMAIN);
                if (permissionCheck == null || !permissionCheck)
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        }
        
        return new ResponseEntity<>(sportelloRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Sportello>> getSportelliDisponibili(@RequestParam String author, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), author + DOMAIN);
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        
        return new ResponseEntity<>(sportelloRepository.getSportelliDisponibili(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sportello> getSportelloById(@PathVariable long id, @RequestParam String author, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), author + DOMAIN);
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        
        Sportello response = sportelloRepository.findById(id).orElse(null);
        if (response == null)
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        else 
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by/{username}")
    public ResponseEntity<List<Sportello>> getSportelliByDocente(@PathVariable String username, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), username + DOMAIN);
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            else {
                permissionCheck = personaRepository.verificaRuolo(UserIdentity.TEACHER, username + DOMAIN);
                if (permissionCheck == null || !permissionCheck)
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        }
        
        return new ResponseEntity<>(sportelloRepository.getSportelliByDocente(username + DOMAIN), HttpStatus.OK);
    }

    @PostMapping("/{id}/subscribe")
    @Transactional
    public ResponseEntity<String> iscriviAlloSportello(@PathVariable long id, @RequestBody Persona user, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), user.getEmail());
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            else {
                permissionCheck = personaRepository.verificaRuolo(UserIdentity.STUDENT, user.getEmail());
                if (permissionCheck == null || !permissionCheck)
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        }
        
        int righeModificate = sportelloRepository.aggiungiIscritto(id);

        if (righeModificate == 0)
            return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
        else {
            righeModificate = sportelloRepository.iscriviAlloSportello(id, user.getEmail());
            if (righeModificate == 0) {
                sportelloRepository.rimuoviIscritto(id);
                return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            else
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.CREATED);
        }
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<String> creaSportello(@RequestBody Sportello datiNuovoSportello, @RequestParam String author, HttpServletRequest request){
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), author + DOMAIN);
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            else {
                permissionCheck = personaRepository.verificaRuolo(UserIdentity.TEACHER, author + DOMAIN);
                if (permissionCheck == null || !permissionCheck) {
                    permissionCheck = personaRepository.verificaRuolo(UserIdentity.ADMIN, author + DOMAIN);
                    
                    if (permissionCheck == null || !permissionCheck)
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    else
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        int modificaCompiuta = sportelloRepository.creaSportello(datiNuovoSportello.getNome_sportello(), datiNuovoSportello.getMax_iscritti(), datiNuovoSportello.getMateria().getId(), datiNuovoSportello.getAula().getId(), datiNuovoSportello.getDocente_responsabile().getEmail());
        if (modificaCompiuta == 0)
            return new ResponseEntity<>("Operazione fallita", HttpStatus.INTERNAL_SERVER_ERROR);
        else 
            return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<Sportello> aggiornaSportello(@RequestBody Sportello datiSportello, @PathVariable long id, @RequestParam String author, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), author + DOMAIN);
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            else {
                permissionCheck = personaRepository.verificaRuolo(UserIdentity.TEACHER, author + DOMAIN);
                if (permissionCheck == null || !permissionCheck) {
                    permissionCheck = personaRepository.verificaRuolo(UserIdentity.ADMIN, author + DOMAIN);

                    if (permissionCheck == null || !permissionCheck)
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    else
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        Sportello sportello = sportelloRepository.findById(id).orElse(null);
        if (sportello == null || sportello.getId_sportello() != id)
            return null;
        else {
            if (!sportello.getNome_sportello().equals(datiSportello.getNome_sportello())) sportello.setNome_sportello(datiSportello.getNome_sportello());
            if (sportello.getMax_iscritti() != datiSportello.getMax_iscritti()) sportello.setMax_iscritti(datiSportello.getMax_iscritti());
            if (sportello.getAula().getId() != datiSportello.getAula().getId()) sportello.setAula(datiSportello.getAula());
            if (sportello.getMateria().getId() != datiSportello.getMateria().getId()) sportello.setMateria(sportello.getMateria());
            if (!sportello.getDocente_responsabile().getEmail().equals(datiSportello.getDocente_responsabile().getEmail())) sportello.setDocente_responsabile(datiSportello.getDocente_responsabile());
            if (!sportello.getGiorni().equals(datiSportello.getGiorni())) sportello.setGiorni(datiSportello.getGiorni());
            return new ResponseEntity<>(sportelloRepository.save(sportello), HttpStatus.ACCEPTED);
        }
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> cancellaSportello(@PathVariable long id, @RequestParam String author, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), author + DOMAIN);
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            else {
                permissionCheck = personaRepository.verificaRuolo(UserIdentity.TEACHER, author + DOMAIN);
                if (permissionCheck == null || !permissionCheck) {
                    permissionCheck = personaRepository.verificaRuolo(UserIdentity.ADMIN, author + DOMAIN);

                    if (permissionCheck == null || !permissionCheck)
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    else
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                }
            }
        }
        
        sportelloRepository.deleteById(id);
        return new ResponseEntity<>("Operazione avvenuta con successo", HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}/unsubscribe")
    @Transactional
    public ResponseEntity<String> disiscriviDalloSportello(@PathVariable long id, @RequestBody Persona persona, HttpServletRequest request) {
        String permission = null;
        if ((permission = request.getHeader("Authorization")) == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        else {
            Boolean permissionCheck = personaRepository.verificaCodice(permission.replace("Bearer ", ""), persona.getEmail());
            if (permissionCheck == null || !permissionCheck)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            else {
                permissionCheck = personaRepository.verificaRuolo(UserIdentity.STUDENT, persona.getEmail());
                if (permissionCheck == null || !permissionCheck)
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        }
        
        int rowsAffected = sportelloRepository.rimuoviIscritto(id);
        if (rowsAffected == 0)
            return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
        else {
            rowsAffected = sportelloRepository.cancellaIscrizione(id, persona.getEmail());
            if (rowsAffected == 0) {
                sportelloRepository.aggiungiIscritto(id);
                return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
            } else 
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.NO_CONTENT);
        }
    }
}
