package org.main.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

// Classe Giorno che utilizza GiornoId come chiave composta
@Entity
@Table(name = "giorno")
public class Giorno {

    @EmbeddedId
    private GiornoId id;
    
    @ManyToOne  
    @MapsId("sportelloId")
    @JoinColumn(name = "id_sportello")
    @JsonIgnore 
    private Sportello sportello;

    public Giorno() {}

    public Giorno(LocalDateTime data_inizio, LocalDateTime data_fine, Long id_sportello) {
        this.id = new GiornoId(data_inizio, data_fine, id_sportello);
        this.sportello = new Sportello();
    }

    public GiornoId getId() {
        return id;
    }

    public void setId(GiornoId id) {
        this.id = id;
    }

    public Sportello getSportello() {
        return sportello;
    }

    public void setSportello(Sportello sportello) {
        this.sportello = sportello;
    }
}
