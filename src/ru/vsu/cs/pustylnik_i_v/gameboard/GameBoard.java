package ru.vsu.cs.pustylnik_i_v.gameboard;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoard {

    private final List<Station> stations;

    public GameBoard() {
        stations = new ArrayList<>();
        stations.add(new Station(0, new ArrayList<>()));
    }


    private void addRoad(Station s1, Station s2, Ticket type) {
        s1.getRoads().add(new Road(s2, type));
        s2.getRoads().add(new Road(s1, type));
    }

    public Station getStation(int number) {
        return stations.get(number);
    }

    public List<Station> getStations() {
        return stations;
    }


    private static void addStation(Station s, Map<Integer, Station> map) {
        map.putIfAbsent(s.getNumber(), s);
    }

    public void initGameBoard(String fileName) {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(fileName)) {
            JSONArray rootJsonArray = (JSONArray) parser.parse(reader);

            Map<Integer, Station> stationsMap = new HashMap<>();

            for (Object object : rootJsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                int number = (int) (long) jsonObject.get("number");
                addStation(new Station(number, new ArrayList<>()), stationsMap);
                Station current = stationsMap.get(number);

                JSONArray neighbourTaxisJsonArray = (JSONArray) jsonObject.get("neighbourTaxis");
                List<Integer> neighbourTaxis = new ArrayList<>();
                for (Object stationNumber : neighbourTaxisJsonArray) {
                    neighbourTaxis.add((int) (long) stationNumber);
                }
                JSONArray neighbourBusesJsonArray = (JSONArray) jsonObject.get("neighbourBuses");
                List<Integer> neighbourBuses = new ArrayList<>();
                for (Object stationNumber : neighbourBusesJsonArray) {
                    neighbourBuses.add((int) (long) stationNumber);
                }
                JSONArray neighbourUndergroundsJsonArray = (JSONArray) jsonObject.get("neighbourUndergrounds");
                List<Integer> neighbourUndergrounds = new ArrayList<>();
                for (Object stationNumber : neighbourUndergroundsJsonArray) {
                    neighbourUndergrounds.add((int) (long) stationNumber);
                }
                JSONArray neigbourWaterJsonArray = (JSONArray) jsonObject.get("neighbourWater");
                List<Integer> neighbourWater = new ArrayList<>();
                if (neigbourWaterJsonArray != null) {
                    for (Object stationNumber : neigbourWaterJsonArray) {
                        neighbourWater.add((int) (long) stationNumber);
                    }
                }

                for (int i : neighbourTaxis) {
                    Station target = new Station(i, new ArrayList<>());
                    addStation(new Station(i, new ArrayList<>()), stationsMap);
                    addStation(target, stationsMap);
                    addRoad(current, target, Ticket.Taxi);
                }
                for (int i : neighbourBuses) {
                    Station target = new Station(i, new ArrayList<>());
                    addStation(new Station(i, new ArrayList<>()), stationsMap);
                    addStation(target, stationsMap);
                    addRoad(current, target, Ticket.Bus);
                }
                for (int i : neighbourUndergrounds) {
                    Station target = new Station(i, new ArrayList<>());
                    addStation(new Station(i, new ArrayList<>()), stationsMap);
                    addStation(target, stationsMap);
                    addRoad(current, target, Ticket.Underground);
                }
                for (int i : neighbourWater) {
                    Station target = new Station(i, new ArrayList<>());
                    addStation(new Station(i, new ArrayList<>()), stationsMap);
                    addStation(target, stationsMap);
                    addRoad(current, target, Ticket.Black);
                }
            }
            stationsMap.forEach(this.stations::add);
        } catch (Exception e) {
            System.err.print("Parsing error:\n" + e);
        }
    }
}
