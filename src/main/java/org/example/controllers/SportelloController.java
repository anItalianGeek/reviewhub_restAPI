package org.example.controllers;

import org.example.controllers.repositories.SportelloRepository;
import org.example.models.Sportello;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/sportello")
public final class SportelloController {
    
    private static final String DOMAIN = "@chilesotti.it";
    
    @Autowired
    private static SportelloRepository sportelloRepository;
    
    public SportelloController(SportelloRepository sportelloRepository){
        this.sportelloRepository = sportelloRepository;
    }
    
    @GetMapping("/all")
    public static List<Sportello> getSportelli() {
        return sportelloRepository.findAll();
    }
    
    @GetMapping("/available")
    public static List<Sportello> getSportelliDisponibili() {
        return sportelloRepository.getSportelliDisponibili();
    }
    
    @GetMapping("/{id}")
    public static Sportello getSportelloById(@PathVariable long id) {
        return sportelloRepository.findById(id).orElse(null);
    }
    
    @GetMapping("/by/{username}")
    public static List<Sportello> getSportelliByDocente(@PathVariable String username){
        return sportelloRepository.getSportelliByDocente(username + DOMAIN);
    }
    
    @PostMapping("/{id}/subscribe")
    @Transactional
    public static Sportello iscriviAlloSportello(@PathVariable long id, @RequestBody String username) {
        int righeModificate = sportelloRepository.aggiungiIscritto(id);
        
        if (righeModificate == 0)
            return null;
        else
            return sportelloRepository.iscriviAlloSportello(id, username + DOMAIN);
    }
    
    @PostMapping("/create")
    @Transactional
    public static Sportello creaSportello(@RequestBody Sportello datiNuovoSportello){
        return sportelloRepository.creaSportello(datiNuovoSportello.getNome_sportello(), datiNuovoSportello.getMax_iscritti(), datiNuovoSportello.getMateria().getId(), datiNuovoSportello.getAula().getId(), datiNuovoSportello.getDocente_responsabile().getEmail());
    }
    
    @PutMapping("/update/{id}")
    @Transactional
    public static Sportello aggiornaSportello(@RequestBody Sportello datiSportello, @PathVariable long id) {
        Sportello sportello = sportelloRepository.findById(id).orElse(null);
        if (sportello == null || sportello.getId_sportello() != id)
            return null;
        else {
            if (!sportello.getNome_sportello().equals(datiSportello.getNome_sportello())) sportello.setNome_sportello(datiSportello.getNome_sportello());
            if (sportello.getMax_iscritti() != datiSportello.getMax_iscritti()) sportello.setMax_iscritti(datiSportello.getMax_iscritti());
            if (sportello.getAula().getId() != datiSportello.getAula().getId()) sportello.setAula(datiSportello.getAula());
            if (sportello.getMateria().getId() != datiSportello.getMateria().getId()) sportello.setMateria(sportello.getMateria());
            if (sportello.getDocente_responsabile().getEmail() != datiSportello.getDocente_responsabile().getEmail()) sportello.setDocente_responsabile(datiSportello.getDocente_responsabile());
            if (!sportello.getGiorni().equals(datiSportello.getGiorni())) sportello.setGiorni(datiSportello.getGiorni());
            sportelloRepository.save(sportello);
        }
    }
    
    @DeleteMapping("/remove/{id}")
    public static void cancellaSportello(@PathVariable long id) {
        sportelloRepository.deleteById(id);
    }
    
    @DeleteMapping("/unsubscribe/{id}")
    @Transactional
    public static void disiscriviDalloSportello(@PathVariable long id, @RequestBody String username) {
        int rowsAffected = sportelloRepository.rimuoviIscritto(id);
        
        if (rowsAffected == 0)
            return;
        else {
            rowsAffected = sportelloRepository.cancellaIscrizione(id, username);
            // return status code
        }
    }
}
