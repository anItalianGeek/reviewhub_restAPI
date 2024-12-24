package org.example.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "giorno")
public class Giorno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "data_inizio")
    private LocalDateTime data_inizio;

    @Column(name = "data_fine")
    private LocalDateTime data_fine;

    @ManyToOne
    @JoinColumn(name = "id_sportello")
    private Sportello sportello;

    public Giorno() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Sportello getSportello() {
        return sportello;
    }

    public void setSportello(Sportello sportello) {
        this.sportello = sportello;
    }

    public LocalDateTime getData_fine() {
        return data_fine;
    }

    public void setData_fine(LocalDateTime data_fine) {
        this.data_fine = data_fine;
    }

    public LocalDateTime getData_inizio() {
        return data_inizio;
    }

    public void setData_inizio(LocalDateTime data_inizio) {
        this.data_inizio = data_inizio;
    }
}
