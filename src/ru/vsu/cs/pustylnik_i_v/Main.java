package ru.vsu.cs.pustylnik_i_v;

import ru.vsu.cs.pustylnik_i_v.view.Window;

import java.util.Locale;

import static java.awt.Frame.MAXIMIZED_BOTH;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        Window mainFrame = new Window();
        mainFrame.setVisible(true);
        mainFrame.setExtendedState(MAXIMIZED_BOTH);
    }
}