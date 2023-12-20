package ru.vsu.cs.pustylnik_i_v.gameboard;

public class Road {
    private final Station target;
    private final Ticket type;

    public Road(Station target, Ticket type) {
        this.target = target;
        this.type = type;
    }

    public Station getTarget() {
        return target;
    }

    public Ticket getType() {
        return type;
    }
}
