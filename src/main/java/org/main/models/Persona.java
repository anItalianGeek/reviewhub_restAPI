package org.main.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "persona")
public class Persona {

    @Id
    @Column(name = "email")
    private String email;

    @Column(name = "nome")
    private String nome;

    @Column(name = "cognome")
    private String cognome;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ruolo")
    private UserIdentity ruolo;

    @Column(name = "classe")
    private String classe;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<AuthToken> authTokens;

    @OneToMany(mappedBy = "docente_responsabile", fetch = FetchType.EAGER)
    private List<Sportello> sportelli;

    @OneToMany(mappedBy = "persona", fetch = FetchType.EAGER)
    private List<IscrizioneSportello> iscrizioni;

    public Persona() {}

    public Persona(String email, String classe, String password, UserIdentity ruolo, String cognome, String nome, List<Sportello> sportelli, List<AuthToken> authTokens, List<IscrizioneSportello> iscrizioni) {
        this.email = email;
        this.classe = classe;
        this.password = password;
        this.ruolo = ruolo;
        this.cognome = cognome;
        this.nome = nome;
        this.sportelli = sportelli;
        this.authTokens = authTokens;
        this.iscrizioni = iscrizioni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Sportello> getSportelli() {
        return sportelli;
    }

    public void setSportelli(List<Sportello> sportelli) {
        this.sportelli = sportelli;
    }

    public List<AuthToken> getAuthTokens() {
        return authTokens;
    }

    public void setAuthTokens(List<AuthToken> authTokens) {
        this.authTokens = authTokens;
    }

    public List<IscrizioneSportello> getIscrizioni() {
        return iscrizioni;
    }

    public void setIscrizioni(List<IscrizioneSportello> iscrizioni) {
        this.iscrizioni = iscrizioni;
    }

    @Override
    public String toString() {
        return "Persona{" +
                "email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", password='" + password + '\'' +
                ", ruolo=" + ruolo +
                ", classe='" + classe + '\'' +
                ", authTokens=" + authTokens +
                ", sportelli=" + sportelli +
                ", iscrizioni=" + iscrizioni +
                '}';
    }
}
