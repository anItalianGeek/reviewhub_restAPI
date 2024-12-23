package org.example.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;

public class Sportello {
    
    private long id_sportello;
    private String nomeSportello;
    private Materia materia;
    private LocalDateTime[] dates;
    private int maxPosti;
    private int numIscritti;
    private LinkedList<String> iscritti;
    private String docenteResponsabile;
    private int aula;
    
    public Sportello() {}

    public String getNomeSportello() {
        return nomeSportello;
    }

    public void setNomeSportello(String nomeSportello) {
        this.nomeSportello = nomeSportello;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public LocalDateTime[] getDates() {
        return dates;
    }

    public void setDates(LocalDateTime[] dates) {
        this.dates = dates;
    }

    public int getMaxPosti() {
        return maxPosti;
    }

    public void setMaxPosti(int maxPosti) {
        this.maxPosti = maxPosti;
    }

    public int getNumIscritti() {
        return numIscritti;
    }

    public void setNumIscritti(int numIscritti) {
        this.numIscritti = numIscritti;
    }

    public String getDocenteResponsabile() {
        return docenteResponsabile;
    }

    public void setDocenteResponsabile(String docenteResponsabile) {
        this.docenteResponsabile = docenteResponsabile;
    }

    public int getAula() {
        return aula;
    }

    public void setAula(int aula) {
        this.aula = aula;
    }

    public LinkedList<String> getIscritti() {
        return iscritti;
    }

    public void setIscritti(LinkedList<String> iscritti) {
        this.iscritti = iscritti;
    }
    
}
