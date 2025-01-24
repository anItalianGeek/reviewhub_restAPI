package org.main.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "iscrizione_sportello")
public class IscrizioneSportello {

    @EmbeddedId
    private IscrizioneSportelloId id;

    @ManyToOne
    @MapsId("sportelloId")
    @JoinColumn(name = "id_sportello")
    @JsonIgnore
    private Sportello sportello;

    @ManyToOne
    @MapsId("personaId")
    @JoinColumn(name = "persona_iscritta")
    @JsonIgnore
    private Persona persona;

    public IscrizioneSportello() {}

    public IscrizioneSportello(Sportello sportello, Persona persona) {
        // Inizializza l'EmbeddedId
        IscrizioneSportelloId iscrizioneId = new IscrizioneSportelloId();
        iscrizioneId.setSportelloId(sportello.getId_sportello());
        iscrizioneId.setPersonaId(persona.getEmail());

        // Configura i campi
        this.id = iscrizioneId;
        this.sportello = sportello;
        this.persona = persona;
    }


    public IscrizioneSportelloId getId() {
        return id;
    }

    public void setId(IscrizioneSportelloId id) {
        this.id = id;
    }
    
}

