package org.example.models.dbmodels;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "aula")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "nome")
    private String nome;

    @OneToMany(mappedBy = "aula")
    private List<SportelloDB> sportelli;

    public Aula() {}
}
