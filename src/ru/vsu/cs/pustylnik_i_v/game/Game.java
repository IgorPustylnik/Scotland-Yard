package ru.vsu.cs.pustylnik_i_v.game;

import ru.vsu.cs.pustylnik_i_v.gameboard.GameBoard;
import ru.vsu.cs.pustylnik_i_v.gameboard.Road;
import ru.vsu.cs.pustylnik_i_v.gameboard.Station;
import ru.vsu.cs.pustylnik_i_v.gameboard.Ticket;
import ru.vsu.cs.pustylnik_i_v.player.Detective;
import ru.vsu.cs.pustylnik_i_v.player.MrX;
import ru.vsu.cs.pustylnik_i_v.player.Player;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;


public class Game {
    public final static String stationsFile = "resources/stations.json";
    public final static String mapFile = "resources/map.png";
    private final Integer DETECTIVES_COUNT = 5;
    private final Integer MRX_COUNT = 1;
    private final Integer PLAYERS_COUNT = DETECTIVES_COUNT + MRX_COUNT;
    private final List<String> possibleNames = new ArrayList<>(Arrays.asList("Игорь", "Виктор", "Геннадий", "Всеволод", "Добромир"));
    private final List<Color> possibleColors = new ArrayList<>(Arrays.asList(
            new Color(0x56FF00), new Color(0x48A6FF), new Color(0xFFD524),
            new Color(0xFF0000), new Color(0xB500E8), new Color(0xD76100)
    ));
    private final boolean debug = false;
    private final List<Integer> MRX_VISIBLE_TURNS = Arrays.asList(3, 8, 13, 18);
    private final double DOUBLE_MOVE_POSSIBILITY = 0.2;
    private final double BLACK_MOVE_POSSIBILITY = 0.2;
    private final Random rnd = new Random();
    private final GameBoard gameBoard;
    private final Queue<Player> players;
    private MrX MrX;
    private List<Station> startTiles;
    private Map<Player, Map<Ticket, Integer>> tickets;
    private Map<Player, Station> playersPositions;
    private Map<Player, Color> playersColors;
    private int globalTurn;
    private int localTurn;
    private Station lastSeenMrX;
    private boolean oneDetectiveCanMove;
    private Player winner;

    public Game() throws Exception {
        gameBoard = new GameBoard();
        players = new LinkedList<>();
        gameBoard.initGameBoard(stationsFile);

        generatePlayers();
        linkColorsToPlayers();
        generateStartTiles();
        generateTickets();
        setPlayersPositions();
        lastSeenMrX = null;
    }

    public ArrayList<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public Player getWinner() {
        return winner;
    }

    public Map<Player, Station> getPlayersPositions() {
        return playersPositions;
    }

    public Map<Player, Color> getPlayersColors() {
        return playersColors;
    }

    public int getLocalTurn() {
        return localTurn;
    }

    public int getGlobalTurn() {
        return globalTurn;
    }

    private void generatePlayers() throws Exception {
        if (possibleNames.size() < players.size()) {
            throw new Exception("Некорректно задан список возможных имён игроков.");
        }
        addMrX();
        Collections.shuffle(possibleNames);
        for (int i = 0; i < DETECTIVES_COUNT; i++) {
            addDetective(possibleNames.get(i));
        }
    }

    private void linkColorsToPlayers() throws Exception {
        playersColors = new HashMap<>();
        if (possibleColors.size() < players.size()) {
            throw new Exception("Некорректно задан список возможных цветов игроков.");
        }
        Collections.shuffle(possibleColors);
        int i = 0;
        for (Player p : players) {
            Color c;
            if (p.isMrX()) {
                c = Color.BLACK;
            } else {
                c = possibleColors.get(i++);
            }
            playersColors.put(p, c);
        }
    }

    private void generateStartTiles() {
        startTiles = new ArrayList<>();
        for (int i = 1; i < 200; i++) {
            startTiles.add(gameBoard.getStation(i));
        }
        Collections.shuffle(startTiles);
    }

    private void generateTickets() {
        tickets = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.poll();
            if (p.isMrX()) {
                Map<Ticket, Integer> currentTickets = new HashMap<>();
                currentTickets.put(Ticket.Taxi, 4);
                currentTickets.put(Ticket.Bus, 3);
                currentTickets.put(Ticket.Underground, 3);
                currentTickets.put(Ticket.Double, 2);
                currentTickets.put(Ticket.Black, DETECTIVES_COUNT);
                this.tickets.put(p, currentTickets);
            } else {
                Map<Ticket, Integer> currentTickets = new HashMap<>();
                currentTickets.put(Ticket.Taxi, 10);
                currentTickets.put(Ticket.Bus, 8);
                currentTickets.put(Ticket.Underground, 4);
                currentTickets.put(Ticket.Double, 0);
                currentTickets.put(Ticket.Black, 0);
                this.tickets.put(p, currentTickets);
            }
            players.add(p);
        }
    }

    private void setPlayersPositions() {
        playersPositions = new HashMap<>();
        int i = 0;
        for (Player p : players) {
            playersPositions.putIfAbsent(p, startTiles.get(i));
            i++;
        }
    }

    private void addDetective(String name) {
        players.add(new Detective(name));
    }

    private void addMrX() {
        MrX = new MrX("Mr. X");
        players.add(MrX);
    }

    private boolean useDoubleTicket() {
        return tickets.get(MrX).get(Ticket.Double) > 0 && (rnd.nextDouble(10) / 10 <= DOUBLE_MOVE_POSSIBILITY);
    }

    private boolean useBlackTicket() {
        double possibility = BLACK_MOVE_POSSIBILITY;
        if (MRX_VISIBLE_TURNS.contains(globalTurn + 1)) {
            possibility += 0.4;
        }
        return tickets.get(MrX).get(Ticket.Black) > 0 && (rnd.nextDouble(10) / 10 <= possibility);
    }

    public boolean mrXVisible() {
        return debug || MRX_VISIBLE_TURNS.contains(globalTurn);
    }

    private String doMove(Player p) {
        StringBuilder log = new StringBuilder();
        int oldStationNumber = playersPositions.get(p).getNumber();
        if (!p.isMrX()) {
            Road way = possibleMoveRoad(p);
            Ticket used = usedTicketToMove(p, way);

            log.append(moveDataToString(p.getName(), oldStationNumber, way, used, true));

            if (used != null) {
                tickets.get(p).put(used, tickets.get(p).get(used) - 1);
                tickets.get(MrX).put(used, tickets.get(MrX).get(used) + 1);
            }
        } else {
            if (useDoubleTicket()) {
                Road way = possibleMoveRoad(p);
                Ticket used1 = usedTicketToMove(p, way);

                log.append(moveDataToString(p.getName(), oldStationNumber, way, used1, mrXVisible()));

                oldStationNumber = playersPositions.get(p).getNumber();
                way = possibleMoveRoad(p);
                Ticket used2 = usedTicketToMove(p, way);

                log.append(moveDataToString(p.getName(), oldStationNumber, way, used2, mrXVisible()));

                lastSeenMrX = mrXVisible() ? playersPositions.get(MrX) : lastSeenMrX;

                tickets.get(p).put(used1, tickets.get(p).get(used1) - 1);
                tickets.get(p).put(used2, tickets.get(p).get(used2) - 1);
                tickets.get(p).put(Ticket.Double, tickets.get(p).get(Ticket.Double) - 1);
            } else {
                Road way = possibleMoveRoad(p);
                Ticket used = usedTicketToMove(p, way);
                used = (useBlackTicket()) ? Ticket.Black : used;
                log.append(moveDataToString(p.getName(), oldStationNumber, way, used, mrXVisible()));

                lastSeenMrX = mrXVisible() ? playersPositions.get(MrX) : lastSeenMrX;

                tickets.get(MrX).put(used, tickets.get(MrX).get(used) - 1);
            }
        }
        return log.toString();
    }

    private static String moveDataToString(String name, int oldStationNumber, Road way, Ticket ticket, boolean visible) {
        Formatter f = new Formatter();
        if (way != null) {
            if (visible) {
                f.format("%s переместился со станции %d на %d, используя %s\n", name, oldStationNumber, way.getTarget().getNumber(), ticket);
            } else {
                f.format("%s переместился, используя %s\n", name, ticket);
            }
        } else {
            f.format("%s не может никуда переместиться\n", name);
        }
        return f.toString();
    }

    private Road smartMove1(Player p) {
        Station from = playersPositions.get(p);
        Station target = lastSeenMrX;

        Map<Station, Boolean> visited = new HashMap<>();
        Map<Station, List<Road>> pathToStation = new HashMap<>();

        List<Road> maxLengthPath = new LinkedList<>();
        for (int i = 0; i < 201; i++) {
            maxLengthPath.add(new Road(null, null));
        }

        gameBoard.getStations().forEach(station -> pathToStation.put(station, maxLengthPath));
        gameBoard.getStations().forEach(station -> visited.put(station, false));

        pathToStation.put(from, new ArrayList<>());

        Queue<Station> queue = new LinkedList<>();
        queue.offer(from);

        while (!queue.isEmpty()) {
            Station currentStation = queue.poll();
            visited.put(currentStation, true);

            for (Road road : currentStation.getRoads()) {
                System.out.print(currentStation.getNumber() + " - ");
                System.out.println(road.getTarget().getNumber());
                Station roadTarget = road.getTarget();

                List<Road> path;
                if (pathToStation.get(currentStation).size() > 199) {
                    path = new ArrayList<>();
                } else {
                    path = new ArrayList<>(pathToStation.get(currentStation));
                }
                path.add(road);
                if (!visited.containsKey(roadTarget) && visited.get(roadTarget) != null) {
                    if (!visited.get(roadTarget) && path.size() < pathToStation.get(roadTarget).size() && !stationIsOccupiedByDetective(roadTarget)) {
                        pathToStation.put(target, path);
                        queue.offer(target);
                    }
                }
            }
        }

        List<List<Road>> paths = new ArrayList<>(pathToStation.values());
        paths.sort(Comparator.comparingInt(List::size));
        paths.forEach(System.out::println);
        for (List<Road> roads : paths) {
            if (!roads.isEmpty()) {
                Road firstOnPath = roads.get(0);
                if (firstOnPath.getType() != null) {
                    continue;
                }
                if (tickets.get(p).get(firstOnPath.getType()) > 0) {
                    return firstOnPath;
                }
            }
        }
        return null;
    }

    private Road smartMove(Player p) {
        Station from = playersPositions.get(p);
        Station target = lastSeenMrX;

        Map<Station, Integer> distance = new HashMap<>();
        Map<Station, Road> previous = new HashMap<>();
        PriorityQueue<Station> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        for (Station station : gameBoard.getStations()) {
            distance.put(station, station == from ? 0 : Integer.MAX_VALUE);
            queue.add(station);
        }

        while (!queue.isEmpty()) {
            Station current = queue.poll();
            if (current == target) {
                return previous.get(target);
            }

            for (Road road : current.getRoads()) {
                Station neighbor = road.getTarget();
                int newDist = distance.get(current) + 1;

                if (newDist < distance.get(neighbor)) {
                    distance.put(neighbor, newDist);
                    previous.put(neighbor, road);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return null;
    }

    private Road randomMove(Player p) {
        Station currentStation = playersPositions.get(p);
        List<Road> roads = new ArrayList<>(currentStation.getRoads());
        Collections.shuffle(roads);
        for (Road road : roads) {
            Ticket requiredTicket = road.getType();
            if (!p.isMrX()) {
                if (tickets.get(p).get(requiredTicket) > 0 && !stationIsOccupiedByDetective(road.getTarget())) {
                    return road;
                }
            } else {
                if ((tickets.get(MrX).get(requiredTicket) > 0 || tickets.get(MrX).get(Ticket.Black) > 0)) {
                    return road;
                }
            }
        }
        return null;
    }

    private Road possibleMoveRoad(Player p) {
        if (!p.isMrX() && lastSeenMrX == null) {
            return randomMove(p);
        } else if (!p.isMrX() && lastSeenMrX != null) {
            return randomMove(p); // должен быть smartMove, но он не работает
        } else {
            return randomMove(p);
        }
    }

    private Ticket usedTicketToMove(Player p, Road way) {
        Ticket used = null;
        if (way != null) {
            Station target = way.getTarget();
            playersPositions.put(p, target);
            used = way.getType();
        }
        return used;
    }

    private boolean stationIsOccupiedByDetective(Station s) {
        return players.stream()
                .filter(p -> !p.isMrX())
                .anyMatch(p -> playersPositions.get(p).equals(s));
    }

    public String playerInfo(Player p) {
        StringBuilder sb = new StringBuilder();
        if (p.isMrX()) {
            if (lastSeenMrX != null) {
                sb.append(String.format(" в последний раз видели на <strong>%d</strong> станции ", lastSeenMrX.getNumber()));
            }
        } else {
            sb.append(String.format(" сейчас на <strong>%d</strong> станции ", playersPositions.get(p).getNumber()));
        }
        return sb.toString();
    }

    public String ticketsInfo(Player p) {
        Map<Ticket, Integer> pTickets = tickets.get(p);
        return String.format("Билеты: Taxi - %d, Bus - %d, Underground - %d, Double - %d, Black - %d\n", pTickets.get(Ticket.Taxi), pTickets.get(Ticket.Bus),
                pTickets.get(Ticket.Underground), pTickets.get(Ticket.Double), pTickets.get(Ticket.Black));
    }

    public void start() {
        globalTurn = 1;
        localTurn = 1;
        winner = null;
        oneDetectiveCanMove = false;
    }

    public String nextTurn() {
        winner = globalTurn == 23 ? MrX : winner;

        if (winner != null) {
            return null;
        }

        if (localTurn == PLAYERS_COUNT + 1) {
            if (!oneDetectiveCanMove) {
                winner = MrX;
            }
            globalTurn++;
            localTurn = 1;
            oneDetectiveCanMove = false;

        }
        localTurn++;

        Player currentPlayer = players.poll();

        assert currentPlayer != null;

        oneDetectiveCanMove = (possibleMoveRoad(currentPlayer) != null && !currentPlayer.isMrX()) || oneDetectiveCanMove;
        String doneMove = doMove(currentPlayer);

        players.add(currentPlayer);
        if (playersPositions.get(currentPlayer) == playersPositions.get(MrX) && !currentPlayer.isMrX()) {
            winner = currentPlayer;
        }

        return doneMove;
    }
}