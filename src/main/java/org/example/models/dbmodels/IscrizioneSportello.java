package org.example.models.dbmodels;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "iscrizione_sportello")
public class IscrizioneSportello {

    @EmbeddedId
    private IscrizioneSportelloId id;

    @ManyToOne
    @MapsId("sportelloId")
    @JoinColumn(name = "id_sportello")
    private SportelloDB sportello;

    @ManyToOne
    @MapsId("personaId")
    @JoinColumn(name = "persona_iscritta")
    private PersonaDB persona;

    public IscrizioneSportello() {}
}

@Embeddable
class IscrizioneSportelloId implements Serializable {
    private Long sportelloId;
    private String personaId;

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
