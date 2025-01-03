package org.main.models.wrappers;

import java.util.LinkedList;

public class Iscrizione {

    private long idSportello;
    private LinkedList<String> iscritti;

    public Iscrizione(long idSportello, LinkedList<String> iscritti) {
        this.iscritti = iscritti;
        this.idSportello = idSportello;
    }

    public long getIdSportello() {
        return idSportello;
    }

    public void setIdSportello(long idSportello) {
        this.idSportello = idSportello;
    }

    public LinkedList<String> getIscritti() {
        return iscritti;
    }

    public void setIscritti(LinkedList<String> iscritti) {
        this.iscritti = iscritti;
    }

}
