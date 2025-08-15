package org.main.v1.models.wrappers;

import org.main.v1.models.Sportello;

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
