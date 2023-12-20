package ru.vsu.cs.pustylnik_i_v.player;

public abstract class Player {
    private final String name;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isMrX() {
       return false;
    }
}
