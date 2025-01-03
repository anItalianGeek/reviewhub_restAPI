package org.main.models.wrappers;

import org.main.models.Sportello;

import java.util.LinkedList;

public class WrapperSportelliStudente {

    private LinkedList<Sportello> sportellos;

    public WrapperSportelliStudente(LinkedList<Sportello> list) {
        sportellos = list;
    }

    public LinkedList<Sportello> getSportellos() {
        return sportellos;
    }

    public void setSportellos(LinkedList<Sportello> sportellos) {
        this.sportellos = sportellos;
    }

}
