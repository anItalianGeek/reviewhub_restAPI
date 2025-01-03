package org.main.models.wrappers;

import org.main.models.Sportello;

import java.util.LinkedList;

public class WrapperSportelliDocente {

    private LinkedList<Sportello> sportellos;
    private LinkedList<Iscrizione> iscritti;

    public WrapperSportelliDocente(LinkedList<Sportello> list, LinkedList<Iscrizione> list2) {
        sportellos = list;
        iscritti = list2;
    }

    public LinkedList<Sportello> getSportellos() {
        return sportellos;
    }

    public void setSportellos(LinkedList<Sportello> sportellos) {
        this.sportellos = sportellos;
    }

    public LinkedList<Iscrizione> getIscritti() {
        return iscritti;
    }

    public void setIscritti(LinkedList<Iscrizione> iscritti) {
        this.iscritti = iscritti;
    }

}
