package fr.chessproject.chessfx.view.components;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;

public class ButtonUtilities {

    public static void loadButtonIcon(Button btn) {
        btn.getStyleClass().add(btn.getId());
        btn.setPickOnBounds(true);

        Region icon = new Region();
        icon.getStyleClass().add("icon");
        btn.setGraphic(icon);
    }
}
