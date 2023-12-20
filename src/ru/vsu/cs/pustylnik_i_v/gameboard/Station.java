package ru.vsu.cs.pustylnik_i_v.gameboard;

import java.util.List;

public class Station {
    private final int number;
    private List<Road> roads;

    public Station(int number, List<Road> roads) {
        this.number = number;
        this.roads = roads;
    }

    public int getNumber() {
        return number;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public void setRoads(List<Road> roads) {
        this.roads = roads;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(number);
        sb.append(": ");
        for (Road road : roads) {
            if (road != null) {
                sb.append(road.getTarget().number);
                sb.append(" - ");
                sb.append(road.getType());
                sb.append("; ");
            }
        }
        return sb.toString();
    }
}
