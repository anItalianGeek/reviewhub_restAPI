package org.main.models;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IscrizioneSportelloId implements Serializable {
    private Long sportelloId;
    private String personaId;

    public IscrizioneSportelloId() {
    }

    public Long getSportelloId() {
        return sportelloId;
    }

    public void setSportelloId(Long sportelloId) {
        this.sportelloId = sportelloId;
    }

    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(String personaId) {
        this.personaId = personaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IscrizioneSportelloId that = (IscrizioneSportelloId) o;
        return Objects.equals(sportelloId, that.sportelloId) && Objects.equals(personaId, that.personaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sportelloId, personaId);
    }
}
