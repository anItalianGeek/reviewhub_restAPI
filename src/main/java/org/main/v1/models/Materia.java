package org.main.v1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "materia")
public class Materia {

    @Id
    @Column(name = "nome")
    private String nome;

    @OneToMany(mappedBy = "materia")
    @JsonIgnore
    private List<Sportello> sportelli;

    public Materia() {}

    public Materia(String nome, List<Sportello> sportelli) {
        this.nome = nome;
        this.sportelli = sportelli;
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
