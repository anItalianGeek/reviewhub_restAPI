package org.main.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "materia")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materia")
    private int id;

    @Column(name = "nome")
    private String nome;

    @OneToMany(mappedBy = "materia")
    private List<Sportello> sportelli;

    public Materia() {}

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Sportello> getSportelli() {
        return sportelli;
    }

    public void setSportelli(List<Sportello> sportelli) {
        this.sportelli = sportelli;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
}
