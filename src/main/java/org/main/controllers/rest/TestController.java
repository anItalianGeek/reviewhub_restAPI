package org.main.controllers.rest;

import org.apache.catalina.User;
import org.main.controllers.repositories.AuthTokenRepository;
import org.main.controllers.repositories.IscrizioneSportelloRepository;
import org.main.controllers.repositories.PersonaRepository;
import org.main.controllers.repositories.SportelloRepository;
import org.main.essentials.AccessData;
import org.main.models.*;
import org.main.models.wrappers.Iscrizione;
import org.main.models.wrappers.WrapperSportelliDocente;
import org.main.other.SHA256Encryptor;
import org.main.other.ServerSignatureGenerator;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
public class TestController {
    
    private static final String DOMAIN = "@chilesotti.it";
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final SportelloRepository sportelloRepository;
    private final IscrizioneSportelloRepository iscrizioneSportelloRepository;
    private final PersonaRepository personaRepository;
    private final AuthTokenRepository authTokenRepository;

    @Autowired
    public TestController(PersonaRepository personaRepository, IscrizioneSportelloRepository iscrizioneSportelloRepository, SportelloRepository sportelloRepository, AuthTokenRepository authTokenRepository) {
        this.personaRepository = personaRepository;
        this.iscrizioneSportelloRepository = iscrizioneSportelloRepository;
        this.sportelloRepository = sportelloRepository;
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
    
}
