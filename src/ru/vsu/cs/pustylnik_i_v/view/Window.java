package ru.vsu.cs.pustylnik_i_v.view;

import ru.vsu.cs.pustylnik_i_v.game.Game;
import ru.vsu.cs.pustylnik_i_v.player.Player;
import ru.vsu.cs.utils.SwingUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;

public class Window extends JFrame {
    private Game game;
    private JMenu menu;
    private JPanel panelMain;
    private JButton buttonNextTurn;
    private JTextArea gameLogTA;
    private MapView mapView;
    private JButton buttonNewGame;
    private ArrayList<Player> playersLinks;
    private JLabel labelPlayersInfo;
    private JScrollPane scrollPaneLog;
    private JLabel labelTicketsInfo;
    private boolean gameOver;

    public Window() {
        this.setTitle("Scotland Yard");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        clearGame();
        mapView.requestFocus();

        labelPlayersInfo.setFont(new Font("Arial", Font.PLAIN, 16));
        labelTicketsInfo.setFont(new Font("Arial", Font.PLAIN, 16));

        buttonNewGame.setFont(new Font("Arial", Font.PLAIN, 24));
        buttonNextTurn.setFont(new Font("Arial", Font.PLAIN, 24));

        setJMenuBar(createMenuBar());
        menu = new JMenu("Look and feel");
        SwingUtils.initLookAndFeelMenu(menu);


        buttonNewGame.addActionListener(e -> {
            clearGame();
            try {
                newGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        buttonNextTurn.addActionListener(e -> {
            String turn = game.nextTurn();

            String old = gameLogTA.getText();
            if (game.getLocalTurn() == 7) {
                turn = new Formatter().format("\nКаждый сделал ход %d раз\n", game.getGlobalTurn()) + turn;
            }

            gameLogTA.setText(turn);
            gameLogTA.append(old);
            updateView();

            SwingUtilities.invokeLater(() -> scrollPaneLog.getVerticalScrollBar().setValue(0));
            gameOver = game.getWinner() != null;
            if (gameOver) {
                String winner = game.getWinner().getName();
                SwingUtils.showInfoMessageBox("Победитель – " + winner + ".", "Игра окончена!");
                try {
                    clearGame();
                    mapView.requestFocus();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        mapView.addKeyListener(new KeyListener() { // изменение масштаба карты
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_EQUALS && (e.isControlDown() || e.isMetaDown())) {
                    mapView.scale *= MapView.scaleChange;
                    mapView.scale = Math.min(MapView.minScale, Math.max(MapView.maxScale, mapView.scale));
                    updateMapSize();
                } else if (e.getKeyCode() == KeyEvent.VK_MINUS && (e.isControlDown() || e.isMetaDown())) {
                    mapView.scale /= MapView.scaleChange;
                    mapView.scale = Math.min(MapView.minScale, Math.max(MapView.maxScale, mapView.scale));
                    updateMapSize();
                } else if (e.getKeyCode() == KeyEvent.VK_0 && (e.isControlDown() || e.isMetaDown())) {
                    mapView.scale = MapView.defaultScale;
                    updateMapSize();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    private void createUIComponents() throws Exception {
        BufferedImage img = ImageIO.read(new File(Game.mapFile));
        mapView = new MapView(img);
        mapView.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        mapView.initVisuals(Game.stationsFile);
        mapView.repaint();
        mapView.requestFocus();
    }

    private void updateMapSize() {
        Dimension scaledImageSize = new Dimension(
                (int) (mapView.image.getWidth(null) * mapView.scale),
                (int) (mapView.image.getHeight(null) * mapView.scale)
        );
        mapView.setPreferredSize(scaledImageSize);
        mapView.revalidate();
    }

    private void linkPlayers() {
        playersLinks = new ArrayList<>();
        playersLinks.addAll(game.getPlayers());
    }

    private void updateView() {
        StringBuilder lb1 = new StringBuilder();
        StringBuilder lb2 = new StringBuilder();
        lb1.append("<html>");
        lb2.append("<html>");
        for (Player p : playersLinks) {
            Color c = game.getPlayersColors().get(p);
            String r = String.format("%02X", c.getRed());
            String g = String.format("%02X", c.getGreen());
            String b = String.format("%02X", c.getBlue());
            lb1.append(String.format("<font color='#%s%s%s'>", r, g, b));
            lb1.append(p.getName());
            while (lb1.length() < 10) {
                lb1.append(" ");
            }
            lb1.append(String.format("</font> %s<br>", game.playerInfo(p)));
            lb2.append(game.ticketsInfo(p)).append("<br>");
        }
        lb1.append("</html>");
        lb2.append("</html>");
        labelPlayersInfo.setText(lb1.toString());
        labelTicketsInfo.setText(lb2.toString());

        mapView.repaint();
        mapView.requestFocus();
    }

    private void newGame() throws Exception {
        game = new Game();
        playersLinks = new ArrayList<>();
        mapView.setGame(game);
        linkPlayers();
        game.start();

        buttonNewGame.setVisible(false);
        buttonNextTurn.setVisible(true);
        labelPlayersInfo.setVisible(true);
        labelTicketsInfo.setVisible(true);
        updateView();

        gameLogTA.setText("Начало игры.");
    }

    private void clearGame() {
        mapView.setGame(null);
        gameLogTA.setText("");
        buttonNewGame.setVisible(true);
        buttonNextTurn.setVisible(false);
        labelPlayersInfo.setVisible(false);
        labelTicketsInfo.setVisible(false);
    }

    private JMenuItem createMenuItem(String text, String shortcut, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(listener);
        if (shortcut != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(shortcut.replace('+', ' ')));
        }
        return menuItem;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBarMain = new JMenuBar();

        JMenu menuGame = new JMenu("Игра");
        menuBarMain.add(menuGame);
        menuGame.add(createMenuItem("Новая", "ctrl+N", e -> {
            try {
                clearGame();
                newGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }));
        menuGame.addSeparator();
        menuGame.add(createMenuItem("Выход", "ctrl+X", e -> {
            System.exit(0);
        }));

        JMenu menuView = new JMenu("Вид");
        menuBarMain.add(menuView);
        menuView.addSeparator();
        SwingUtils.initLookAndFeelMenu(menuView);

        JMenu menuHelp = new JMenu("Справка");
        menuBarMain.add(menuHelp);
        menuHelp.add(createMenuItem("Как играть", "ctrl+R", e -> {
            SwingUtils.showInfoMessageBox("Игра умеет играть сама в себя.\nОт вас требуется лишь нажимать кнопку \"Следующий ход\"!", "Как играть");
        }));
        menuHelp.add(createMenuItem("О программе", "ctrl+A", e -> {
            SwingUtils.showInfoMessageBox(
                    """
                            О программе
                            Игра Scotland Yard (Преступник и детективы)
                            Автор: Пустыльник И.В"""
            );
        }));

        return menuBarMain;
    }
}
