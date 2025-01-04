package org.main.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Classe Giorno che utilizza GiornoId come chiave composta
@Entity
@Table(name = "giorno")
public class Giorno {

    @EmbeddedId
    private GiornoId id;

    @Column(name = "data_inizio")
    @MapsId("data_inizioId")
    private LocalDateTime data_inizio;
    
    @Column(name = "data_fine")
    @MapsId("data_fineId")
    private LocalDateTime data_fine;
    
    @ManyToOne  
    @MapsId("sportelloId")
    @JoinColumn(name = "id_sportello")
    private Sportello sportello;

    public Giorno() {}

    public Giorno(LocalDateTime data_inizio, LocalDateTime data_fine, Sportello id_sportello) {
        this.id = new GiornoId(data_inizio, data_fine, id_sportello);
        this.sportello = new Sportello();
    }

    public GiornoId getId() {
        return id;
    }

    public void setId(GiornoId id) {
        this.id = id;
    }

    public LocalDateTime getData_inizio() {
        return id.getData_inizioId();
    }

    public void setData_inizio(LocalDateTime data_inizio) {
        this.id.setData_inizioId(data_inizio);
    }

    public LocalDateTime getData_fine() {
        return id.getData_fineId();
    }

    public void setData_fine(LocalDateTime data_fine) {
        this.id.setData_fineId(data_fine);
    }

    public Sportello getSportello() {
        return sportello;
    }

    public void setSportello(Sportello sportello) {
        this.sportello = sportello;
    }
}
