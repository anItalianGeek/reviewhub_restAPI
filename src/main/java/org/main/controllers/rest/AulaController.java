package org.main.controllers.rest;

import org.main.controllers.repositories.AulaRepository;
import org.main.models.Aula;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/aula")
public class AulaController {
    
    private final AulaRepository aulaRepository;
    
    @Autowired
    public AulaController(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }
    
    @Async("requestHandler")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Aula>>> getTutteAule() {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(aulaRepository.findAll()));
    }
    
    @Async("requestHandler")
    @PostMapping
    public CompletableFuture<ResponseEntity<Void>> aggiungiAula(@RequestBody Aula nuovaAula) {
        return CompletableFuture.supplyAsync(() -> {
            aulaRepository.save(nuovaAula);
            return new ResponseEntity<>(HttpStatus.CREATED);
        });
    }
    
    @Async("requestHandler")
    @PutMapping
    public CompletableFuture<ResponseEntity<Void>> aggiornaAula(@RequestBody Aula aula){
        return CompletableFuture.supplyAsync(() -> {
            aulaRepository.save(aula);
            return new ResponseEntity<>(HttpStatus.OK);
        });
    }
    
    @Async("requestHandler")
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> cancellaAula(@PathVariable int id){
        return CompletableFuture.supplyAsync(() -> {
           aulaRepository.deleteById(id);
           return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        });
    }
    
}
