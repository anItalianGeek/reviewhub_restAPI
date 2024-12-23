package org.example.models.dbmodels;

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
    private SportelloDB sportello;

    public Giorno() {}
}
