package org.example.models;

import org.example.interfaces.Interactable;

public class Persona implements Interactable {
    
    protected String nome;
    protected String cognome;
    protected String email;
    protected String classe;
    protected UserIdentity ruolo;

    public Persona() {}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserIdentity getRuolo() {
        return ruolo;
    }

    public void setRuolo(UserIdentity ruolo) {
        this.ruolo = ruolo;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    @Override
    public String getFullName() {
        return nome + " " + cognome;
    }

    @Override
    public String getFullData() {
        return nome + " " + cognome + " " + email + " " + classe;
    }
    
}
