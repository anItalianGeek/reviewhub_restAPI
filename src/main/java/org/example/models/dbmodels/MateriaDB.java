package org.example.models.dbmodels;

import org.example.models.Materia;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "materia")
public class MateriaDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materia")
    private long id;

    @Column(name = "nome")
    private String nome;

    @OneToMany(mappedBy = "materia")
    private List<SportelloDB> sportelli;

    public MateriaDB() {}
}
