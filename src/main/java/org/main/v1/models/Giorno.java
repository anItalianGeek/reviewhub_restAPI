package org.main.v1.models;

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

    @Column(name = "max_iscritti")
    private int max_iscritti;

    @Column(name = "num_iscritti")
    private int num_iscritti;
    
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

    public int getNum_iscritti() {
        return num_iscritti;
    }

    public void setNum_iscritti(int num_iscritti) {
        this.num_iscritti = num_iscritti;
    }

    public int getMax_iscritti() {
        return max_iscritti;
    }

    public void setMax_iscritti(int max_iscritti) {
        this.max_iscritti = max_iscritti;
    }
}
