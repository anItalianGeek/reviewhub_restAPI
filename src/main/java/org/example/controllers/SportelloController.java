package org.example.controllers;

import org.example.controllers.repositories.SportelloRepository;
import org.example.models.dbmodels.SportelloDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sportello")
public class SportelloController {
    
    private static final String DOMAIN = "@chilesotti.it";
    
    @Autowired
    private SportelloRepository sportelloRepository;
    
    public SportelloController(SportelloRepository sportelloRepository){
        this.sportelloRepository = sportelloRepository;
    }
    
    @GetMapping("/all")
    public List<SportelloDB> getSportelli() {
        return sportelloRepository.findAll();
    }
    
    @GetMapping("/available")
    public List<SportelloDB> getSportelliDisponibili() {
        return sportelloRepository.getSportelliDisponibili();
    }
    
    @GetMapping("/{id}")
    public SportelloDB getSportelloById(@PathVariable long id) {
        return sportelloRepository.findById(id).orElse(null);
    }
    
    @GetMapping("/by/{username}")
    public List<SportelloDB> getSportelliByDocente(@PathVariable String username){
        return sportelloRepository.getSportelliByDocente(username + DOMAIN);
    }
}
