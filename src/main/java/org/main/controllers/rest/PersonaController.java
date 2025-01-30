package org.main.controllers.rest;

import org.main.controllers.repositories.AuthTokenRepository;
import org.main.controllers.repositories.PersonaRepository;
import org.main.controllers.repositories.SportelloRepository;
import org.main.models.AuthToken;
import org.main.models.Persona;
import org.main.essentials.AccessData;
import org.main.models.Sportello;
import org.main.models.UserIdentity;
import org.main.models.wrappers.Iscrizione;
import org.main.models.wrappers.IscrizioneDetail;
import org.main.other.SHA256Encryptor;
import org.main.other.ServerSignatureGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/users")
public class PersonaController {

    private static final Logger logger = LoggerFactory.getLogger(PersonaController.class);
    
    private static final String DOMAIN = "@chilesotti.it";
    private final PersonaRepository personaRepository;
    private final AuthTokenRepository authTokenRepository;
    private final SportelloRepository sportelloRepository;

    @Autowired
    public PersonaController(PersonaRepository personaRepository, AuthTokenRepository authTokenRepository, SportelloRepository sportelloRepository) {
        this.personaRepository = personaRepository;
        this.authTokenRepository = authTokenRepository;
        this.sportelloRepository = sportelloRepository;
    }

    @Async("requestHandler")
    @GetMapping("/test")
    public CompletableFuture<ResponseEntity<String>> test() {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok("Test Successful!"));
    }

    @Async("requestHandler")
    @PostMapping("/check")
    public CompletableFuture<ResponseEntity<Boolean>> getDisponibilitaEmail(@RequestBody String requestedEmail) {
        return CompletableFuture.supplyAsync(() -> {
            Boolean databaseResponse = personaRepository.verificaMailDisponibile(requestedEmail);
            if (databaseResponse == null || !databaseResponse)
                return ResponseEntity.ok(false);
            else
                return ResponseEntity.ok(true);
        });
    }
    
    @Async("requestHandler")
    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<List<IscrizioneDetail>>> getTuttePersone() {
        return CompletableFuture.supplyAsync(() -> {
            
            List<Persona> list = personaRepository.findAll();
            LinkedList<IscrizioneDetail> iscrizioni = new LinkedList<>();
            
            list.forEach(e -> {
                LinkedList<Long> ids = new LinkedList<>();
                e.getIscrizioni().forEach(id -> ids.add(id.getId().getSportelloId()));
                e.getSportelli().forEach(s -> {
                    s.getAula().setSportelli(null);
                    s.getMateria().setSportelli(null);
                    s.getDocente_responsabile().setPassword(null);
                    s.getDocente_responsabile().setAuthTokens(null);
                    s.getDocente_responsabile().setSportelli(null);
                    s.getDocente_responsabile().setIscrizioni(null);
                });
                
                List<Sportello> sportelli = sportelloRepository.findAllById(ids.stream().toList());
                sportelli.forEach(s -> {
                    s.getAula().setSportelli(null);
                    s.getMateria().setSportelli(null);
                    s.getDocente_responsabile().setPassword(null);
                    s.getDocente_responsabile().setAuthTokens(null);
                    s.getDocente_responsabile().setSportelli(null);
                    s.getDocente_responsabile().setIscrizioni(null);
                    s.setIscrizioni(null);
                });
                
                iscrizioni.add(new IscrizioneDetail(e, sportelli));
            });
            
            return new ResponseEntity<>(iscrizioni.stream().toList(), HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @GetMapping("/{username}")
    public CompletableFuture<ResponseEntity<Persona>> getPersonaById(@PathVariable String username) {
        return CompletableFuture.supplyAsync(() -> {
            Persona persona = personaRepository.findById(username + DOMAIN).orElse(null);
            if (persona == null)
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            persona.getSportelli().forEach(s -> s.setDocente_responsabile(null));
            return ResponseEntity.ok(persona);
        });
    }


    @Async("requestHandler")
    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<AccessData>> accedi(@RequestBody Persona persona) {
        return CompletableFuture.supplyAsync(() -> {
            String token = SHA256Encryptor.encrypt(ServerSignatureGenerator.generateSignature());
            authTokenRepository.save(new AuthToken(token, persona));
            return new ResponseEntity<>(new AccessData(token, personaRepository.ottieniRuolo(persona.getEmail())), HttpStatus.CREATED);
        });
    }

    @Async("requestHandler")
    @DeleteMapping("logout")
    public CompletableFuture<ResponseEntity<Boolean>> disconnetti(@RequestParam String author){
        return CompletableFuture.supplyAsync(() -> {
            authTokenRepository.logout(author + DOMAIN);
            if (authTokenRepository.esistonoToken(author + DOMAIN) != 0)
                throw new RuntimeException("Logout Fallito");
            else 
                return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
        });
    }
    
    @Async("requestHandler")
    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<String>> aggiungiPersona(@RequestBody Persona datiNuovaPersona, @RequestParam String author) {
        return CompletableFuture.supplyAsync(() -> {
            Persona userActionPerformer = personaRepository.findById(author).orElse(null);
            if (userActionPerformer != null) {
                if (!userActionPerformer.getRuolo().equals(UserIdentity.ADMIN) && !datiNuovaPersona.getRuolo().equals(UserIdentity.STUDENT))
                    return new ResponseEntity<>("Impossibile creare un utente con i privilegi indicati. Permessi insufficenti.", HttpStatus.FORBIDDEN);
            } else if (!datiNuovaPersona.getRuolo().equals(UserIdentity.STUDENT))
                return new ResponseEntity<>("Impossibile creare un utente con i privilegi indicati. Permessi insufficenti.", HttpStatus.FORBIDDEN);
            
            personaRepository.save(
                    new Persona(
                            datiNuovaPersona.getEmail(),
                            datiNuovaPersona.getClasse(),
                            datiNuovaPersona.getPassword(),
                            datiNuovaPersona.getRuolo(),
                            datiNuovaPersona.getCognome(),
                            datiNuovaPersona.getNome(),
                            null,
                            null,
                            null
                    ));
            return new ResponseEntity<>("Operazione compiuta con successo.", HttpStatus.CREATED);
        });
    }

    @Async("requestHandler")
    @PutMapping("/modify/{user}")
    public CompletableFuture<ResponseEntity<Persona>> aggiornaPersona(@RequestBody Persona datiPersona, @PathVariable String user) {
        return CompletableFuture.supplyAsync(() -> {
            Persona persona = personaRepository.findById(user + DOMAIN).orElse(null);

            if (persona == null || !(user + DOMAIN).equals(persona.getEmail()))
                return null;
            else if (!persona.getEmail().split("@")[0].equals(user) && !persona.getRuolo().equals(UserIdentity.ADMIN)) { /* CASO ECCEZZIONALE DI CONTROLLO DENTRO AL CONTROLLER */
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            else {
                if (!persona.getEmail().equals(datiPersona.getEmail()))
                    persona.setEmail(datiPersona.getEmail());
                if (!persona.getNome().equals(datiPersona.getNome()))
                    persona.setNome(datiPersona.getNome());
                if (!persona.getCognome().equals(datiPersona.getCognome()))
                    persona.setCognome(datiPersona.getCognome());
                if (!persona.getPassword().equals(datiPersona.getPassword()))
                    persona.setPassword(datiPersona.getPassword());
                if (!persona.getRuolo().equals(datiPersona.getRuolo()))
                    persona.setRuolo(datiPersona.getRuolo());
                if (!persona.getClasse().equals(datiPersona.getClasse()))
                    persona.setClasse(datiPersona.getClasse());

                return new ResponseEntity<>(personaRepository.save(persona), HttpStatus.ACCEPTED);
            }
        });
    }

    @Async("requestHandler")
    @DeleteMapping("/remove/{user}")
    public CompletableFuture<ResponseEntity<String>> cancellaPersona(@PathVariable String user) {
        return CompletableFuture.supplyAsync(() -> {
            int rowsAffected = personaRepository.cancellaPersona(user + DOMAIN);
            if (rowsAffected == 0)
                return new ResponseEntity<>("Operazione fallita o senza effetto.", HttpStatus.NOT_FOUND);
            else
                return new ResponseEntity<>("Operazione compiuta.", HttpStatus.NO_CONTENT);
        });
    }

}
