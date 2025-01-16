package org.main.controllers.rest;

import com.google.gson.GsonBuilder;
import org.main.configuration.security.SecurityConfig;
import org.main.controllers.repositories.AuthTokenRepository;
import org.main.controllers.repositories.PersonaRepository;
import org.main.models.AuthToken;
import org.main.models.Persona;
import org.main.models.UserIdentity;
import org.main.other.SHA256Encryptor;
import org.main.other.ServerSignatureGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class PersonaController {

    private static final Logger logger = LoggerFactory.getLogger(PersonaController.class);
    
    private final AsyncTaskExecutor requestHandler;
    private static final String DOMAIN = "@chilesotti.it";
    private final PersonaRepository personaRepository;
    private final AuthTokenRepository authTokenRepository;

    @Autowired
    public PersonaController(@Qualifier("requestHandler") AsyncTaskExecutor taskExecutor, PersonaRepository personaRepository, AuthTokenRepository authTokenRepository) {
        this.requestHandler = taskExecutor;
        this.personaRepository = personaRepository;
        this.authTokenRepository = authTokenRepository;
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
    public CompletableFuture<ResponseEntity<List<Persona>>> getTuttePersone() {
        return CompletableFuture.supplyAsync(() -> new ResponseEntity<>(personaRepository.findAll(), HttpStatus.OK));
    }
    

    @Async("requestHandler")
    @GetMapping("/{username}")
    public CompletableFuture<ResponseEntity<Persona>> getPersonaById(@PathVariable String username) {
        return CompletableFuture.supplyAsync(() -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Persona persona = personaRepository.findById(username + DOMAIN).orElse(null);
            if (authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")).contains("ROLE_STUDENT"))
                return ResponseEntity.ok(new Persona(persona.getEmail(), persona.getClasse(), persona.getPassword(), persona.getRuolo(), persona.getCognome(), persona.getNome(), null, null, null));
            else if (authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")).contains("ROLE_ADMIN"))
                return ResponseEntity.ok(persona);
            else 
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }, requestHandler);
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
    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<String>> aggiungiPersona(@RequestBody Persona datiNuovaPersona) {
        return CompletableFuture.supplyAsync(() -> {
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
    @Transactional
    public CompletableFuture<ResponseEntity<Persona>> aggiornaPersona(@RequestBody Persona datiPersona, @PathVariable String user) {
        return CompletableFuture.supplyAsync(() -> {
            Persona persona = personaRepository.findById(user + DOMAIN).orElse(null);

            if (persona == null || !(user + DOMAIN).equals(persona.getEmail()))
                return null;
            else {
                if (!persona.getNome().equals(datiPersona.getNome())) persona.setNome(datiPersona.getNome());
                if (!persona.getCognome().equals(datiPersona.getCognome()))
                    persona.setCognome(datiPersona.getCognome());
                if (!persona.getPassword().equals(datiPersona.getPassword()))
                    persona.setPassword(datiPersona.getPassword());
                if (!persona.getClasse().equals(datiPersona.getClasse())) persona.setClasse(datiPersona.getClasse());
                if (!persona.getSportelli().equals(datiPersona.getSportelli()))
                    persona.setSportelli(datiPersona.getSportelli());
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

    public static String getSingleRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

}

class AccessData {

    private String token;
    private UserIdentity ruolo;

    public AccessData(String token, UserIdentity ruolo) {
        this.ruolo = ruolo;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserIdentity getRuolo() {
        return ruolo;
    }

    public void setRuolo(UserIdentity ruolo) {
        this.ruolo = ruolo;
    }

}
