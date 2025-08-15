package org.main.v1.controllers.rest;

import org.main.v1.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/test")
public class TestController {

    public TestController() {
    }
    
    @Async("requestHandler")
    @GetMapping
    public CompletableFuture<ResponseEntity<String>> test() {
        return CompletableFuture.supplyAsync(() -> new ResponseEntity<>("Test Successfull", HttpStatus.OK));
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
