package org.main.controllers.rest;

import jakarta.transaction.Transactional;
import org.main.controllers.repositories.AuthTokenRepository;
import org.main.controllers.repositories.PersonaRepository;
import org.main.models.Persona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/test")
public class TestController {

    private final String DOMAIN = "@chilesotti.it";
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final AsyncTaskExecutor requestHandler;
    private final PersonaRepository personaRepository;
    private final AuthTokenRepository authTokenRepository;

    @Autowired
    public TestController(@Qualifier("requestHandler") AsyncTaskExecutor taskExecutor, PersonaRepository personaRepository, AuthTokenRepository authTokenRepository) {
        this.requestHandler = taskExecutor;
        this.personaRepository = personaRepository;
        this.authTokenRepository = authTokenRepository;
    }
    
    @Async("requestHandler")
    @GetMapping
    public CompletableFuture<ResponseEntity<String>> test() {
        return CompletableFuture.supplyAsync(() -> new ResponseEntity<>("Test Successfull", HttpStatus.OK));
    }
    
    @Async("requestHandler")
    @GetMapping("/with-auth")
    public CompletableFuture<ResponseEntity<String>> test_auth() {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok("Test Successfull"));
    }
    
    @GetMapping("/no-async")
    public ResponseEntity<String> test_no_async() {
        return ResponseEntity.ok("Test Successfull");
    }
    
    @PostMapping
    public CompletableFuture<ResponseEntity<Persona>> test_post() {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(new Persona()));
    }



    @Async("requestHandler")
    @GetMapping("/{username}")
    @Transactional
    public CompletableFuture<ResponseEntity<Persona>> getPersonaById(@PathVariable String username) {
        logger.info("Controller: Utente autenticato prima dell'@Async: {}", SecurityContextHolder.getContext().getAuthentication());

        return CompletableFuture.supplyAsync(() -> {
            logger.info("Asincrono: Utente autenticato: {}", SecurityContextHolder.getContext().getAuthentication());
            Persona persona = personaRepository.findById(username + DOMAIN).orElse(null);
            logger.info("Asincrono: Risultato trovato: {}", persona != null ? persona.getEmail() : "Nessuna persona trovata");
            return ResponseEntity.ok(new Persona(persona.getEmail(), persona.getClasse(), persona.getPassword(), persona.getRuolo(), persona.getCognome(), persona.getNome(), null, null, null));
            //return ResponseEntity.ok(persona);
        }, requestHandler); // Passiamo esplicitamente il task executor
    }
    
}
