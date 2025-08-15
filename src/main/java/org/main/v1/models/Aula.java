package org.main.v1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
@Entity
@Table(name = "aula")
public class Aula {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "nome")
    private String nome;

    @OneToMany(mappedBy = "aula")
    @JsonIgnore
    private List<Sportello> sportelli;

    public Aula() {}

    public Aula(int id, String nome, List<Sportello> sportelli) {
        this.id = id;
        this.nome = nome;
        this.sportelli = sportelli;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Sportello> getSportelli() {
        return sportelli;
    }

    public void setSportelli(List<Sportello> sportelli) {
        this.sportelli = sportelli;
    }
    
}
