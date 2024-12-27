package org.main.models;// Versione aggiornata

import jakarta.persistence.*;
import org.springframework.cglib.core.Local;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

// Classe Embeddable per rappresentare la chiave composta
@Embeddable
class GiornoId implements Serializable {

    @Column(name = "data_inizio")
    private LocalDateTime data_inizio;

    @Column(name = "data_fine")
    private LocalDateTime data_fine;

    @Column(name = "id_sportello")
    private Long sportelloId;

    // Costruttore, getter, setter, equals e hashCode
    public GiornoId() {}

    public GiornoId(LocalDateTime data_inizio, LocalDateTime data_fine, Long sportelloId) {
        this.data_inizio = data_inizio;
        this.data_fine = data_fine;
        this.sportelloId = sportelloId;
    }

    public LocalDateTime getData_inizio() {
        return data_inizio;
    }

    public void setData_inizio(LocalDateTime data_inizio) {
        this.data_inizio = data_inizio;
    }

    public LocalDateTime getData_fine() {
        return data_fine;
    }

    public void setData_fine(LocalDateTime data_fine) {
        this.data_fine = data_fine;
    }

    public Long getSportelloId() {
        return sportelloId;
    }

    public void setSportelloId(Long sportelloId) {
        this.sportelloId = sportelloId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiornoId giornoId = (GiornoId) o;
        return Objects.equals(data_inizio, giornoId.data_inizio) &&
                Objects.equals(data_fine, giornoId.data_fine) &&
                Objects.equals(sportelloId, giornoId.sportelloId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data_inizio, data_fine, sportelloId);
    }
}

// Classe Giorno che utilizza GiornoId come chiave composta
@Entity
@Table(name = "giorno")
public class Giorno {

    @EmbeddedId
    private GiornoId id;

    @ManyToOne
    @MapsId("sportelloId")
    @JoinColumn(name = "id_sportello")
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

    public LocalDateTime getData_inizio() {
        return id.getData_inizio();
    }

    public void setData_inizio(LocalDateTime data_inizio) {
        this.id.setData_inizio(data_inizio);
    }

    public LocalDateTime getData_fine() {
        return id.getData_fine();
    }

    public void setData_fine(LocalDateTime data_fine) {
        this.id.setData_fine(data_fine);
    }

    public Sportello getSportello() {
        return sportello;
    }

    public void setSportello(Sportello sportello) {
        this.sportello = sportello;
    }
}
