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
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

                UserIdentity ruoloAttuale = personaRepository.ottieniRuolo(author + DOMAIN);
                if (ruoloAttuale.equals(UserIdentity.STUDENT) || (!response.getDocente_responsabile().getEmail().split("@")[0].equals(author) && !ruoloAttuale.equals(UserIdentity.ADMIN))) {
                    sportellos.forEach(sportello -> sportello.setIscrizioni(null));
                    List<Object[]> tutteIscrizioniUtente = sportelloRepository.getIscrizioniNelloSportello(response.getId_sportello(), author + DOMAIN);
                    
                    tutteIscrizioniUtente.forEach(iscrizione -> {
                        LinkedList<String> personeIscritte = new LinkedList<>();
                        Persona persona = (Persona) iscrizione[0]; 
                        personeIscritte.add(
                                persona.getNome() + " " + persona.getCognome() + " [Classe: " + persona.getClasse() +  "] (" + persona.getEmail() + ")"
                        );
                        iscrizioni.add(new Iscrizione((GiornoId) iscrizione[1], personeIscritte));
                    });
                } else {
                    List<Object[]> tutteIscrizioni = sportelloRepository.getIscrizioniNelloSportello(response.getId_sportello());
                    
                    HashMap<GiornoId, List<Persona>> iscrizioniHashmap = new HashMap<>();
                    tutteIscrizioni.forEach(iscrizione -> {
                        Persona persona = (Persona) iscrizione[0];
                        GiornoId giornoId = (GiornoId) iscrizione[1];

                        iscrizioniHashmap.putIfAbsent(giornoId, new LinkedList<>());
                        iscrizioniHashmap.get(giornoId).add(persona);
                    });


                    iscrizioniHashmap.keySet().forEach(giornoId -> {
                        LinkedList<String> personeIscritte = new LinkedList<>();
                        iscrizioniHashmap.get(giornoId).forEach(persona -> personeIscritte.add(persona.getNome() + " " + persona.getCognome() + " [Classe: " + persona.getClasse() +  "] (" + persona.getEmail() + ")"));
                        iscrizioni.add(new Iscrizione(giornoId, personeIscritte));
                    });
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
                sportello.getAula().setSportelli(null);
                sportello.getMateria().setSportelli(null);
                sportello.getDocente_responsabile().setPassword(null);
                sportello.getDocente_responsabile().setIscrizioni(null);
                sportello.getDocente_responsabile().setSportelli(null);
                sportello.getDocente_responsabile().setAuthTokens(null);

                List<Object[]> tutteIscrizioni = sportelloRepository.getIscrizioniNelloSportello(sportello.getId_sportello());

                HashMap<GiornoId, List<Persona>> iscrizioniHashmap = new HashMap<>();
                tutteIscrizioni.forEach(iscrizione -> {
                    Persona persona = (Persona) iscrizione[0];
                    GiornoId giornoId = (GiornoId) iscrizione[1];
                    if (iscrizioniHashmap.containsKey(giornoId))
                        iscrizioniHashmap.get(giornoId).add(persona);
                    else
                        iscrizioniHashmap.put(giornoId, new LinkedList<>());
                });

                iscrizioniHashmap.keySet().forEach(giornoId -> {
                    LinkedList<String> personeIscritte = new LinkedList<>();
                    iscrizioniHashmap.get(giornoId).forEach(persona -> personeIscritte.add(persona.getNome() + " " + persona.getCognome() + " [Classe: " + persona.getClasse() +  "] (" + persona.getEmail() + ")"));
                    iscrizioni.add(new Iscrizione(giornoId, personeIscritte));
                });
            }

            WrapperSportelliDocente wrapper = new WrapperSportelliDocente(new LinkedList<>(sportelli), iscrizioni);
            return new ResponseEntity<>(wrapper, HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @PostMapping("/subscribe/{id}")
    public CompletableFuture<ResponseEntity<String>> iscriviAlloSportello(@PathVariable long id, @RequestParam String author, @RequestBody GiornoId giornoId) {
        return CompletableFuture.supplyAsync(() -> {
            Giorno giorno = giornoRepository.findById(giornoId).orElse(null);
            if (giorno == null)
                return new ResponseEntity<>("Giornata/e non trovata/e", HttpStatus.NOT_FOUND);
            else {
                giornoRepository.aggiungiIscritto(giornoId);
                Sportello sportello = sportelloRepository.findById(id).orElse(null);
                Persona persona = personaRepository.findById(author + DOMAIN).orElse(null);
                if (persona == null || sportello == null)
                    return new ResponseEntity<>("Utente o sportello non trovato", HttpStatus.NOT_FOUND);
                iscrizioneSportelloRepository.save(new IscrizioneSportello(sportello, persona, giorno));
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
            if (giorni == null || giorni.isEmpty())
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
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
                if (sportello.getAula().getId() != datiSportello.getAula().getId()) sportello.setAula(datiSportello.getAula());
                if (!sportello.getMateria().getNome().equals(datiSportello.getMateria().getNome())) sportello.setMateria(sportello.getMateria());
                if (!sportello.getDocente_responsabile().getEmail().equals(datiSportello.getDocente_responsabile().getEmail())) sportello.setDocente_responsabile(datiSportello.getDocente_responsabile());
                if (!sportello.getGiorni().equals(datiSportello.getGiorni())) {
                    List<Giorno> giorni = datiSportello.getGiorni();
                    sportello.setGiorni(null);
                    //giornoRepository.cancellaGiorni(id);
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
    @PostMapping("/unsubscribe/{id}")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> disiscriviDalloSportello(@PathVariable long id, @RequestParam String author, @RequestBody GiornoId giornoId) {
        return CompletableFuture.supplyAsync(() -> {
            iscrizioneSportelloRepository.cancellaIscrizione(id, author + DOMAIN, giornoId.getData_inizioId(), giornoId.getData_fineId());
            giornoRepository.rimuoviIscritto(giornoId);
            if (iscrizioneSportelloRepository.esisteIscrizione(id, author + DOMAIN, giornoId.getData_inizioId(), giornoId.getData_fineId()) != 1)
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.NO_CONTENT);
            else
                throw new RuntimeException("Operazione fallita.");
        });
    }
    
    @Async("requestHandler")
    @DeleteMapping("/remove/{id}")
    public CompletableFuture<ResponseEntity<String>> cancellaSportello(@PathVariable long id) {
        return CompletableFuture.supplyAsync(() -> {
            iscrizioneSportelloRepository.cancellaTutteIscrizioni(id);
            giornoRepository.cancellaGiorni(id);
            sportelloRepository.deleteById(id);
            return new ResponseEntity<>("Operazione avvenuta con successo", HttpStatus.NO_CONTENT);
        });
    }

    @Async("requestHandler")
    @PostMapping("/{id}/remove-subscription/{username}")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> rimuoviIscritto(@PathVariable long id, @PathVariable String username, @RequestBody GiornoId giornoId){
        return CompletableFuture.supplyAsync(() -> {
            iscrizioneSportelloRepository.cancellaIscrizione(id, username + DOMAIN, giornoId.getData_inizioId(), giornoId.getData_fineId());
            if (iscrizioneSportelloRepository.esisteIscrizione(id, username + DOMAIN, giornoId.getData_inizioId(), giornoId.getData_fineId()) != 1) {
                giornoRepository.rimuoviIscritto(giornoId);
                return new ResponseEntity<>("Operazione Compiuta con Successo", HttpStatus.NO_CONTENT);
            }
            else 
                return new ResponseEntity<>("Operazione fallita", HttpStatus.BAD_REQUEST);
        });
    }
    
}
