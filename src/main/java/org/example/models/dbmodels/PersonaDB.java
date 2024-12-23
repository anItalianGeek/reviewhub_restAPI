package org.example.models.dbmodels;

import org.example.models.Persona;
import org.example.models.UserIdentity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "persona")
public class PersonaDB {

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

    @OneToMany(mappedBy = "docente_responsabile")
    private List<SportelloDB> sportelli;

    public PersonaDB() {}
}
