package fr.chessproject.chessfx.view.components;

import fr.chessproject.chessfx.model.game.ClockModel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ClockView {

    private static final double THRESHOLD = 10.0;

    private final Label label;

    public ClockView(StackPane root) {
        label = new Label();
        StackPane.setAlignment(label, Pos.CENTER);
        root.getChildren().add(label);
    }

    public void update(ClockModel model) {
        double remainingTime = model.getRemainingTime();

        int min = (int) Math.floor(remainingTime / 60);
        int sec = (int) remainingTime % 60;
        String message;

        if (remainingTime < THRESHOLD) {
            int tenths = (int) ((remainingTime - Math.floor(remainingTime)) * 10);
            message = String.format("%02d:%02d.%d", min, sec, tenths);
            label.setTextFill(model.isTicking() ? Color.rgb(227,88,105) : Color.GREY);
        } else {
            message = String.format("%02d:%02d", min, sec);
            label.setTextFill(model.isTicking() ? Color.WHITE : Color.GREY);
        }

        label.setText(message);
    }
}
