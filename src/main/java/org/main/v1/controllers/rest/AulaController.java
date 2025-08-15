package org.main.v1.controllers.rest;

import org.main.v1.controllers.repositories.AulaRepository;
import org.main.v1.models.Aula;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/aula")
public class AulaController {
    
    private final AulaRepository aulaRepository;
    
    @Autowired
    public AulaController(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }
    
    @Async("requestHandler")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Aula>>> getTutteAule(@RequestParam int offset, @RequestParam(required = false) Integer limit) {
        return CompletableFuture.supplyAsync(() -> {
            Pageable pageable;

            if (limit == null) pageable = PageRequest.of(offset, 20);
            else pageable = PageRequest.of(offset, limit);

            return ResponseEntity.ok(aulaRepository.findAllByPage(pageable).getContent());
        });
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
