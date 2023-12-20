package ru.vsu.cs.pustylnik_i_v.view;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.vsu.cs.pustylnik_i_v.game.Game;
import ru.vsu.cs.pustylnik_i_v.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapView extends JPanel {
    public static final double defaultScale = 1.0;
    public static final double minScale = 1.5;
    public static final double maxScale = 0.44;
    public static final double scaleChange = 1.08;
    protected final Image image;
    protected double scale;
    private Game game;
    private Map<Integer, Point> stations;
    private ArrayList<Player> playersLinks;

    public MapView(Image image) {
        this.image = image;
        this.game = null;
        this.stations = new HashMap<>();
        this.playersLinks = new ArrayList<>();
        scale = 1.0;
    }

    public void setGame(Game game) {
        this.game = game;
        if (game != null) {
            linkPlayers();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.scale(scale, scale);
        g2d.drawImage(image, 0, 0, this);
        if (game != null) {
            for (Player p : playersLinks) {
                if (!p.isMrX()) {
                    int playerStationNumber = game.getPlayersPositions().get(p).getNumber();
                    g2d.setColor(game.getPlayersColors().get(p));
                    Point station = stations.get(playerStationNumber);
                    int x = station.getX() - 20;
                    int y = station.getY() + 35;
                    g2d.setStroke(new BasicStroke(10));
                    g2d.drawOval(x - 5, y - 60, 50, 50);
                    g2d.setColor(Color.BLACK); //
//                    g2d.setStroke(new BasicStroke(2));
//                    g2d.drawString(p.getName(), x - 10, y + 10); // отображение имени игрока
                }
            }
        }
        g2d.dispose();
    }

    private void linkPlayers() {
        playersLinks = new ArrayList<>();
        playersLinks.addAll(game.getPlayers());
    }

    public void initVisuals(String fileName) {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(fileName)) {
            JSONArray rootJsonArray = (JSONArray) parser.parse(reader);

            for (Object object : rootJsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                int number = (int) (long) jsonObject.get("number");

                JSONObject guiCoordinatesJsonArray = (JSONObject) jsonObject.get("guiCoordinates");
                int x = (int) (long) guiCoordinatesJsonArray.get("x");
                int y = (int) (long) guiCoordinatesJsonArray.get("y");

                stations.put(number, new Point(x, y));
            }
        } catch (Exception e) {
            System.err.print("Parsing error:\n" + e);
        }
    }
}