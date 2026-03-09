package fr.chessproject.chessfx.helpers;

import javafx.stage.Screen;

public class Constants {
    public static final double SCREEN_SIZE = Screen.getPrimary().getVisualBounds().getHeight();
    public static final double BOARD_SIZE = Screen.getPrimary().getVisualBounds().getHeight() * 0.80;
    public static final int REAL_BOARD_SIZE = (int) ((BOARD_SIZE / 8) * 8);
}
