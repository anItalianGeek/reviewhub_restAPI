package org.main.v1.models.wrappers;

import org.main.v1.models.GiornoId;

import java.util.LinkedList;

public class Iscrizione {

    private GiornoId id;
    private LinkedList<String> iscritti;

    public Iscrizione(GiornoId id, LinkedList<String> iscritti) {
        this.id = id;
        this.iscritti = iscritti;
    }

    public LinkedList<String> getIscritti() {
        return iscritti;
    }

    public void setIscritti(LinkedList<String> iscritti) {
        this.iscritti = iscritti;
    }

    public GiornoId getId() {
        return id;
    }

    public void setId(GiornoId id) {
        this.id = id;
    }
}
