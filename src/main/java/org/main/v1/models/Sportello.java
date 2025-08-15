package org.main.v1.models;

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

    @Column(name = "descrizione_sportello")
    private String descrizione_sportello;

    @ManyToOne
    @JoinColumn(name = "aula_id")
    private Aula aula;

    @ManyToOne
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @Column(name = "sportello_disponibile")
    private boolean sportello_disponibile;
    
    @ManyToOne
    @JoinColumn(name = "docente_responsabile")
    private Persona docente_responsabile;

    @OneToMany(mappedBy = "sportello", fetch = FetchType.EAGER)
    private List<IscrizioneSportello> iscrizioni;

    @OneToMany(mappedBy = "sportello", fetch = FetchType.EAGER)
    private List<Giorno> giorni;

    public Sportello() {}

    public Sportello(long id_sportello, String nome_sportello, String descrizione_sportello, Persona docente_responsabile, Materia materia, Aula aula, List<Giorno> giorni, List<IscrizioneSportello> iscrizioni) {
        this.id_sportello = id_sportello;
        this.docente_responsabile = docente_responsabile;
        this.materia = materia;
        this.aula = aula;
        this.nome_sportello = nome_sportello;
        this.descrizione_sportello = descrizione_sportello;
        this.giorni = giorni;
        this.iscrizioni = iscrizioni;
    }

    public String getNome_sportello() {
        return nome_sportello;
    }

    public String getDescrizione_sportello() {
        return descrizione_sportello;
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

    public boolean isSportello_disponibile() {
        return sportello_disponibile;
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

    public void setDescrizione_sportello(String descrizione_sportello) {
        this.descrizione_sportello = descrizione_sportello;
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

    public void setSportello_disponibile(boolean sportello_disponibile) {
        this.sportello_disponibile = sportello_disponibile;
    }

    public List<IscrizioneSportello> getIscrizioni() {
        return iscrizioni;
    }

    public void setIscrizioni(List<IscrizioneSportello> iscrizioni) {
        this.iscrizioni = iscrizioni;
    }
}
