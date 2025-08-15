package org.main.v1.controllers.rest;

import org.main.v1.controllers.repositories.MateriaRepository;
import org.main.v1.models.Materia;
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
@RequestMapping("/api/v1/materia")
public class MateriaController {

    private final MateriaRepository materiaRepository;

    @Autowired
    public MateriaController(MateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    @Async("requestHandler")
    @GetMapping
    public CompletableFuture<ResponseEntity<List<Materia>>> getTutteAule(@RequestParam int offset, @RequestParam(required = false) Integer limit) {
        return CompletableFuture.supplyAsync(() -> {
            Pageable pageable;

            if (limit == null) pageable = PageRequest.of(offset, 20);
            else pageable = PageRequest.of(offset, limit);

            return ResponseEntity.ok(materiaRepository.findAllByPage(pageable).getContent());
        });
    }

    @Async("requestHandler")
    @PostMapping
    public CompletableFuture<ResponseEntity<Void>> aggiungiAula(@RequestBody Materia nuovaMateria) {
        return CompletableFuture.supplyAsync(() -> {
            materiaRepository.save(nuovaMateria);
            return new ResponseEntity<>(HttpStatus.CREATED);
        });
    }

    @Async("requestHandler")
    @PutMapping
    public CompletableFuture<ResponseEntity<Void>> aggiornaAula(@RequestBody Materia materia){
        return CompletableFuture.supplyAsync(() -> {
            materiaRepository.save(materia);
            return new ResponseEntity<>(HttpStatus.OK);
        });
    }

    @Async("requestHandler")
    @DeleteMapping("/{materia}")
    public CompletableFuture<ResponseEntity<Void>> cancellaAula(@PathVariable String materia){
        return CompletableFuture.supplyAsync(() -> {
            materiaRepository.deleteById(materia);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        });
    }

}
