package org.main.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "sportello")
public class Sportello {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sportello")
    private long id_sportello;

    @Column(name = "nome_sportello")
    private String nome_sportello;

    @Column(name = "max_iscritti")
    private int max_iscritti;

    @Column(name = "num_ipscritti")
    private int num_iscritti;

    @ManyToOne
    @JoinColumn(name = "aula_id")
    private Aula aula;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @ManyToOne
    @JoinColumn(name = "docente_responsabile")
    private Persona docente_responsabile;

    @OneToMany(mappedBy = "sportello")
    private List<Giorno> giorni;

    public Sportello() {}

    public Sportello(long id_sportello, Persona docente_responsabile, Materia materia, Aula aula, int num_iscritti, int max_iscritti, String nome_sportello, List<Giorno> giorni) {
        this.id_sportello = id_sportello;
        this.docente_responsabile = docente_responsabile;
        this.materia = materia;
        this.aula = aula;
        this.num_iscritti = num_iscritti;
        this.max_iscritti = max_iscritti;
        this.nome_sportello = nome_sportello;
        this.giorni = giorni;
    }

    public String getNome_sportello() {
        return nome_sportello;
    }

    public int getMax_iscritti() {
        return max_iscritti;
    }

    public int getNum_iscritti() {
        return num_iscritti;
    }

    public Aula getAula() {
        return aula;
    }

    public Materia getMateria() {
        return materia;
    }

    public Persona getDocente_responsabile() {
        return docente_responsabile;
    }

    public List<Giorno> getGiorni() {
        return giorni;
    }

    public long getId_sportello() {
        return id_sportello;
    }

    public void setId_sportello(long id_sportello) {
        this.id_sportello = id_sportello;
    }

    public void setNome_sportello(String nome_sportello) {
        this.nome_sportello = nome_sportello;
    }

    public void setMax_iscritti(int max_iscritti) {
        this.max_iscritti = max_iscritti;
    }

    public void setNum_iscritti(int num_iscritti) {
        this.num_iscritti = num_iscritti;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public void setDocente_responsabile(Persona docente_responsabile) {
        this.docente_responsabile = docente_responsabile;
    }

    public void setGiorni(List<Giorno> giorni) {
        this.giorni = giorni;
    }
}
