package org.example.models.dbmodels;

import org.example.models.Materia;
import org.example.models.Persona;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "sportello")
public class SportelloDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sportello")
    private long id_sportello;

    @Column(name = "nome_sportello")
    private String nome_sportello;

    @Column(name = "max_iscritti")
    private int max_iscritti;

    @Column(name = "num_iscritti")
    private int num_iscritti;

    @ManyToOne
    @JoinColumn(name = "aula_id")
    private Aula aula;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private MateriaDB materia;

    @ManyToOne
    @JoinColumn(name = "docente_responsabile_email")
    private PersonaDB docente_responsabile;

    @OneToMany(mappedBy = "sportello")
    private List<Giorno> giorni;

    public SportelloDB() {}
}
