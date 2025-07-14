package fr.chessproject.chessfx.view;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private final Theme theme;
    private final List<Theme> themesList;

    public Config() {
        themesList = new ArrayList<>();
        addThemes();
        theme = themesList.getFirst();
    }

    private void addThemes() {
        Theme lichess = new Theme(Color.rgb(240, 217, 181), Color.rgb(181, 136, 99), Color.rgb(113, 217, 100), Color.rgb(50, 200, 100), Color.rgb(246, 235, 114), Color.rgb(220, 195, 75), Color.rgb(246, 235, 114), Color.rgb(246, 235, 114), Color.rgb(220, 195, 75), Color.rgb(220, 195, 75), Color.rgb(235, 121, 99), Color.rgb(225, 105, 84), Color.rgb(242, 234, 183),  Color.rgb(250, 242, 229), Color.rgb(229, 213, 201));
        Theme codingAdventure = new Theme(Color.rgb(238,216,192), Color.rgb(171,122,101), Color.rgb(113, 217, 100), Color.rgb(50, 200, 100), Color.rgb(236,197,123), Color.rgb(200,158,80), Color.rgb(207,172,106), Color.rgb(221,207,124), Color.rgb(197,158,94), Color.rgb(197,173,96), Color.rgb(89,171,221), Color.rgb(62,144,195), Color.rgb(242, 234, 183),  Color.rgb(250, 242, 229), Color.rgb(229, 213, 201));

        themesList.add(codingAdventure);
        themesList.add(lichess);
    }

    public Theme getTheme() {
        return theme;
    }
}
