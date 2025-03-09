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
    
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "data_inizio_giorno_ref", referencedColumnName = "data_inizio"),
            @JoinColumn(name = "data_fine_giorno_ref", referencedColumnName = "data_fine"),
            @JoinColumn(name = "sportello_ref_id", referencedColumnName = "id_sportello")
    })
    @MapsId("giornoId")
    @JsonIgnore
    private Giorno giorno;

    public IscrizioneSportello() {}

    public IscrizioneSportello(Sportello sportello, Persona persona, Giorno giorno) {
        // Inizializza l'EmbeddedId
        IscrizioneSportelloId iscrizioneId = new IscrizioneSportelloId();
        iscrizioneId.setSportelloId(sportello.getId_sportello());
        iscrizioneId.setPersonaId(persona.getEmail());
        iscrizioneId.setGiornoId(giorno.getId());
        
        // Configura i campi
        this.id = iscrizioneId;
        this.sportello = sportello;
        this.persona = persona;
        this.giorno = giorno;
    }


    public IscrizioneSportelloId getId() {
        return id;
    }

    public void setId(IscrizioneSportelloId id) {
        this.id = id;
    }
    
}

