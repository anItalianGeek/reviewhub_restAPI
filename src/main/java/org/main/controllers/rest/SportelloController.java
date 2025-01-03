package org.main.controllers.rest;

import org.main.controllers.repositories.PersonaRepository;
import org.main.controllers.repositories.SportelloRepository;
import org.main.models.wrappers.Iscrizione;
import org.main.models.wrappers.WrapperSportelliDocente;
import org.main.models.Persona;
import org.main.models.Sportello;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/sportello")
public class SportelloController {

    private static final String DOMAIN = "@chilesotti.it";
    private final SportelloRepository sportelloRepository;

    @Autowired
    public SportelloController(SportelloRepository sportelloRepository){
        this.sportelloRepository = sportelloRepository;
    }
    
    @Async("requestHandler")
    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<List<Sportello>>> getSportelli() {
        return CompletableFuture.supplyAsync(() -> new ResponseEntity<>(sportelloRepository.findAll(), HttpStatus.OK));
    }

    @Async("requestHandler")
    @GetMapping("/available")
    public CompletableFuture<ResponseEntity<List<Sportello>>> getSportelliDisponibili() {
        return CompletableFuture.supplyAsync(() -> new ResponseEntity<>(sportelloRepository.getSportelliDisponibili(), HttpStatus.OK));
    }

    @Async("requestHandler")
    @GetMapping("/subscribed")
    public CompletableFuture<ResponseEntity<List<Sportello>>> getSportelliPrenotati(@RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> new ResponseEntity<>(sportelloRepository.getSportelliPrenotati(author + DOMAIN), HttpStatus.OK));
    }
    
    @Async("requestHandler")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<WrapperSportelliDocente>> getSportelloById(@PathVariable long id) {
        return CompletableFuture.supplyAsync(() -> {
            Sportello response = sportelloRepository.findById(id).orElse(null);
            if (response == null)
                return new ResponseEntity<>(new WrapperSportelliDocente(null, null), HttpStatus.NOT_FOUND);
            else {
                LinkedList<Iscrizione> iscrizioni = new LinkedList<>();
                LinkedList<Sportello> sportellos = new LinkedList<>();
                sportellos.add(response);
                
                if (!getSingleRole().equals("STUDENT")) {
                    List<Persona> iscrittiAlloSportello = sportelloRepository.getIscrittiNelloSportello(response.getId_sportello());
                    LinkedList<String> iscritti = new LinkedList<>();
                    for (Persona persona : iscrittiAlloSportello)
                        iscritti.add(persona.getNome() + " " + persona.getCognome() + " (" + persona.getEmail() + ")");
                    iscrizioni.add(new Iscrizione(response.getId_sportello(), iscritti));
                }
                return new ResponseEntity<>(new WrapperSportelliDocente(sportellos, iscrizioni), HttpStatus.OK);
            }
        });
    }

    @Async("requestHandler")
    @GetMapping("/by/{author}")
    public CompletableFuture<ResponseEntity<WrapperSportelliDocente>> getSportelliByDocente(@PathVariable String author) {
        return CompletableFuture.supplyAsync(() -> {
            List<Sportello> sportelli = sportelloRepository.getSportelliByDocente(author + DOMAIN);
            LinkedList<Iscrizione> iscrizioni = new LinkedList<>();
            for (Sportello sportello : sportelli) {
                List<Persona> iscrittiAlloSportello = sportelloRepository.getIscrittiNelloSportello(sportello.getId_sportello());
                LinkedList<String> iscritti = new LinkedList<>();
                for (Persona persona : iscrittiAlloSportello)
                    iscritti.add(persona.getNome() + " " + persona.getCognome() + " (" + persona.getEmail() + ")");
                iscrizioni.add(new Iscrizione(sportello.getId_sportello(), iscritti));
            }
            WrapperSportelliDocente wrapper = new WrapperSportelliDocente((LinkedList<Sportello>) sportelli, iscrizioni);
            return new ResponseEntity<>(wrapper, HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @PostMapping("/subscribe/{id}")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> iscriviAlloSportello(@PathVariable long id, @RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> {
            int righeModificate = sportelloRepository.aggiungiIscritto(id);

            if (righeModificate == 0)
                return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
            else {
                righeModificate = sportelloRepository.iscriviAlloSportello(id, author + DOMAIN);
                if (righeModificate == 0) {
                    sportelloRepository.rimuoviIscritto(id);
                    return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                else
                    return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.CREATED);
            }
        });
    }

    @Async("requestHandler")
    @PostMapping("/create")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> creaSportello(@RequestBody Sportello datiNuovoSportello) {
        return CompletableFuture.supplyAsync(() -> {
            int modificaCompiuta = sportelloRepository.creaSportello(datiNuovoSportello.getNome_sportello(), datiNuovoSportello.getMax_iscritti(), datiNuovoSportello.getMateria().getId(), datiNuovoSportello.getAula().getId(), datiNuovoSportello.getDocente_responsabile().getEmail());
            if (modificaCompiuta == 0)
                return new ResponseEntity<>("Operazione fallita", HttpStatus.INTERNAL_SERVER_ERROR);
            else
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.CREATED);
        });
    }

    @Async("requestHandler")
    @PutMapping("/modify/{id}")
    @Transactional
    public CompletableFuture<ResponseEntity<Sportello>> aggiornaSportello(@RequestBody Sportello datiSportello, @PathVariable long id) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    @Async("requestHandler")
    @DeleteMapping("/remove/{id}")
    public CompletableFuture<ResponseEntity<String>> cancellaSportello(@PathVariable long id) {
        return CompletableFuture.supplyAsync(() -> {
            sportelloRepository.deleteById(id);
            return new ResponseEntity<>("Operazione avvenuta con successo", HttpStatus.NO_CONTENT);
        });
    }
    
    @Async("requestHandler")
    @DeleteMapping("/{id}/remove-subscription/{username}")
    public CompletableFuture<ResponseEntity<String>> rimuoviIscritto(@PathVariable long id, @PathVariable String username){
        return CompletableFuture.supplyAsync(() -> {
            int rowsAffected = sportelloRepository.rimuoviIscritto(id);
            if (rowsAffected == 0)
                return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
            else {
                rowsAffected = sportelloRepository.cancellaIscrizione(id, username + DOMAIN);
                if (rowsAffected == 0) {
                    sportelloRepository.aggiungiIscritto(id);
                    return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
                } else
                    return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.NO_CONTENT);
            }
        });
    }
    
    @Async("requestHandler")
    @DeleteMapping("/unsubscribe/{id}")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> disiscriviDalloSportello(@PathVariable long id, @RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> {
            int rowsAffected = sportelloRepository.rimuoviIscritto(id);
            if (rowsAffected == 0)
                return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
            else {
                rowsAffected = sportelloRepository.cancellaIscrizione(id, author + DOMAIN);
                if (rowsAffected == 0) {
                    sportelloRepository.aggiungiIscritto(id);
                    return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
                } else
                    return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.NO_CONTENT);
            }
        });
    }

    public static String getSingleRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }
    
}
