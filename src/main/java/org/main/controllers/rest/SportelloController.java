package org.main.controllers.rest;

import org.main.controllers.repositories.GiornoRepository;
import org.main.controllers.repositories.IscrizioneSportelloRepository;
import org.main.controllers.repositories.PersonaRepository;
import org.main.controllers.repositories.SportelloRepository;
import org.main.models.*;
import org.main.models.wrappers.Iscrizione;
import org.main.models.wrappers.WrapperSportelliDocente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/sportello")
public class SportelloController {

    private static final Logger logger = LoggerFactory.getLogger(SportelloController.class);
    private static final String DOMAIN = "@chilesotti.it";
    private final SportelloRepository sportelloRepository;
    private final IscrizioneSportelloRepository iscrizioneSportelloRepository;
    private final PersonaRepository personaRepository;
    private final GiornoRepository giornoRepository;
    
    @Autowired
    public SportelloController(SportelloRepository sportelloRepository, IscrizioneSportelloRepository iscrizioneSportelloRepository, PersonaRepository personaRepository, GiornoRepository giornoRepository){
        this.sportelloRepository = sportelloRepository;
        this.iscrizioneSportelloRepository = iscrizioneSportelloRepository;
        this.personaRepository = personaRepository;
        this.giornoRepository = giornoRepository;
    }

    @Async("requestHandler")
    @GetMapping("/test")
    public CompletableFuture<ResponseEntity<String>> test() {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok("Test Successful!"));
    }
    
    @Async("requestHandler")
    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<List<Sportello>>> getSportelli() {
        return CompletableFuture.supplyAsync(() -> {
            List<Sportello> list = sportelloRepository.findAll();
            list.forEach(e -> {
                e.getAula().setSportelli(null);
                e.getMateria().setSportelli(null);
                e.getDocente_responsabile().setPassword(null);
                e.getDocente_responsabile().setAuthTokens(null);
                e.getDocente_responsabile().setSportelli(null);
                e.getDocente_responsabile().setIscrizioni(null);
            });
            return new ResponseEntity<>(list, HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @GetMapping("/available")
    public CompletableFuture<ResponseEntity<List<Sportello>>> getSportelliDisponibili() {
        return CompletableFuture.supplyAsync(() -> {
            List<Sportello> list = sportelloRepository.getSportelliDisponibili();
            list.forEach(e -> {
                e.getAula().setSportelli(null);
                e.getMateria().setSportelli(null);
                e.getDocente_responsabile().setPassword(null);
                e.getDocente_responsabile().setAuthTokens(null);
                e.getDocente_responsabile().setSportelli(null);
                e.getDocente_responsabile().setIscrizioni(null);
                e.setIscrizioni(null);
            });

            return new ResponseEntity<>(list, HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @GetMapping("/subscribed")
    public CompletableFuture<ResponseEntity<List<Sportello>>> getSportelliPrenotati(@RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> {
            List<Sportello> list = sportelloRepository.getSportelliPrenotati(author + DOMAIN);
            list.forEach(e -> {
                e.getAula().setSportelli(null);
                e.getMateria().setSportelli(null);
                e.getDocente_responsabile().setPassword(null);
                e.getDocente_responsabile().setAuthTokens(null);
                e.getDocente_responsabile().setSportelli(null);
                e.getDocente_responsabile().setIscrizioni(null);
                e.setIscrizioni(null);
            });

            return new ResponseEntity<>(list, HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<WrapperSportelliDocente>> getSportelloById(@PathVariable long id, @RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> {
            Sportello response = sportelloRepository.findById(id).orElse(null);
            if (response == null)
                return new ResponseEntity<>(new WrapperSportelliDocente(null, null), HttpStatus.NOT_FOUND);
            else {
                LinkedList<Iscrizione> iscrizioni = new LinkedList<>();
                LinkedList<Sportello> sportellos = new LinkedList<>();

                response.getAula().setSportelli(null);
                response.getMateria().setSportelli(null);
                response.getDocente_responsabile().setPassword(null);
                response.getDocente_responsabile().setIscrizioni(null);
                response.getDocente_responsabile().setSportelli(null);
                response.getDocente_responsabile().setAuthTokens(null);
                sportellos.add(response);

                if (personaRepository.ottieniRuolo(author + DOMAIN).equals(UserIdentity.STUDENT)) {
                    sportellos.getFirst().setIscrizioni(null);
                } else {
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
                sportello.getAula().setSportelli(null);
                sportello.getMateria().setSportelli(null);
                sportello.getDocente_responsabile().setPassword(null);
                sportello.getDocente_responsabile().setIscrizioni(null);
                sportello.getDocente_responsabile().setSportelli(null);
                sportello.getDocente_responsabile().setAuthTokens(null);
                LinkedList<String> iscritti = new LinkedList<>();
                for (Persona persona : iscrittiAlloSportello)
                    iscritti.add(persona.getNome() + " " + persona.getCognome() + " (" + persona.getEmail() + ")");
                iscrizioni.add(new Iscrizione(sportello.getId_sportello(), iscritti));
            }

            WrapperSportelliDocente wrapper = new WrapperSportelliDocente(new LinkedList<>(sportelli), iscrizioni);
            return new ResponseEntity<>(wrapper, HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @PostMapping("/subscribe/{id}")
    public CompletableFuture<ResponseEntity<String>> iscriviAlloSportello(@PathVariable long id, @RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> {
            int righeModificate = sportelloRepository.aggiungiIscritto(id);

            if (righeModificate == 0)
                return new ResponseEntity<>("Operazione Fallita", HttpStatus.INTERNAL_SERVER_ERROR);
            else {
                Sportello sportello = sportelloRepository.findById(id).orElse(null);
                Persona persona = personaRepository.findById(author + DOMAIN).orElse(null);
                if (persona == null || sportello == null)
                    return new ResponseEntity<>("Operazione Fallita.", HttpStatus.NOT_FOUND);
                iscrizioneSportelloRepository.save(new IscrizioneSportello(sportello, persona));
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.CREATED);
            }
        });
    }

    /** METODO REVISIONATO IN SEGUITO ATTEGIAMENTI FROCI DA PARTE DI SPRING */
    @Async("requestHandler")
    @PostMapping("/create")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> creaSportello(@RequestBody Sportello datiNuovoSportello) {
        return CompletableFuture.supplyAsync(() -> {
            // Rimuovi i giorni temporaneamente
            List<Giorno> giorni = datiNuovoSportello.getGiorni();
            datiNuovoSportello.setGiorni(null);

            // Salva lo sportello senza giorni
            Sportello sportelloCaricato = sportelloRepository.save(datiNuovoSportello);

            // Persisti ogni giorno separatamente e collegalo allo sportello
            for (Giorno giorno : giorni) {
                giorno.setSportello(sportelloCaricato);
                giornoRepository.save(giorno); // Salva ogni giorno individualmente
            }

            return new ResponseEntity<>("Operazione Completata", HttpStatus.CREATED);
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
                if (!sportello.getDescrizione_sportello().equals(datiSportello.getDescrizione_sportello())) sportello.setDescrizione_sportello(datiSportello.getDescrizione_sportello());
                if (sportello.getMax_iscritti() != datiSportello.getMax_iscritti()) sportello.setMax_iscritti(datiSportello.getMax_iscritti());
                if (sportello.getAula().getId() != datiSportello.getAula().getId()) sportello.setAula(datiSportello.getAula());
                if (!sportello.getMateria().getNome().equals(datiSportello.getMateria().getNome())) sportello.setMateria(sportello.getMateria());
                if (!sportello.getDocente_responsabile().getEmail().equals(datiSportello.getDocente_responsabile().getEmail())) sportello.setDocente_responsabile(datiSportello.getDocente_responsabile());
                if (!sportello.getGiorni().equals(datiSportello.getGiorni())) {
                    List<Giorno> giorni = datiSportello.getGiorni();
                    sportello.setGiorni(null);
                    giornoRepository.cancellaGiorni(id);
                    giorni.forEach(g -> {
                        g.setSportello(sportello);
                        g.getId().setSportelloId(sportello.getId_sportello());
                        giornoRepository.save(g);
                    });
                }

                Sportello defSportello = sportelloRepository.save(sportello);
                defSportello.setGiorni(datiSportello.getGiorni());
                defSportello.getDocente_responsabile().setSportelli(null);
                defSportello.getDocente_responsabile().setPassword(null);
                defSportello.getDocente_responsabile().setAuthTokens(null);
                defSportello.getDocente_responsabile().setIscrizioni(null);
                return new ResponseEntity<>(defSportello, HttpStatus.ACCEPTED);
            }
        });
    }

    @Async("requestHandler")
    @DeleteMapping("/remove/{id}")
    public CompletableFuture<ResponseEntity<String>> cancellaSportello(@PathVariable long id) {
        return CompletableFuture.supplyAsync(() -> {
            giornoRepository.cancellaGiorni(id);
            iscrizioneSportelloRepository.cancellaTutteIscrizioni(id);
            sportelloRepository.deleteById(id);
            return new ResponseEntity<>("Operazione avvenuta con successo", HttpStatus.NO_CONTENT);
        });
    }

    @Async("requestHandler")
    @DeleteMapping("/{id}/remove-subscription/{username}")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> rimuoviIscritto(@PathVariable long id, @PathVariable String username){
        return CompletableFuture.supplyAsync(() -> {
            sportelloRepository.rimuoviIscritto(id);
            iscrizioneSportelloRepository.cancellaIscrizione(id, username + DOMAIN);
            if (iscrizioneSportelloRepository.esisteIscrizione(id, username + DOMAIN) != 1)
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.NO_CONTENT);
            else 
                throw new RuntimeException("Operazione fallita.");
        });
    }

    @Async("requestHandler")
    @DeleteMapping("/unsubscribe/{id}")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> disiscriviDalloSportello(@PathVariable long id, @RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> {
            sportelloRepository.rimuoviIscritto(id);
            iscrizioneSportelloRepository.cancellaIscrizione(id, author + DOMAIN);
            if (iscrizioneSportelloRepository.esisteIscrizione(id, author + DOMAIN) != 1)
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.NO_CONTENT);
            else
                throw new RuntimeException("Operazione fallita.");
        });
    }
    
}
