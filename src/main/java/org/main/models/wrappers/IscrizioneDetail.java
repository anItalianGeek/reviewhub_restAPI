package org.main.models.wrappers;

import org.main.models.Persona;
import org.main.models.Sportello;

import java.util.List;

public class IscrizioneDetail {
    private Persona persona;
    private List<Sportello> sportelli;

    public IscrizioneDetail(Persona persona, List<Sportello> sportello) {
        this.persona = persona;
        this.sportelli = sportello;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public List<Sportello> getSportelli() {
        return sportelli;
    }

    public void setSportelli(List<Sportello> sportello) {
        this.sportelli = sportello;
    }
}
