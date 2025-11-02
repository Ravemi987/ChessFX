package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.model.PieceIndex;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class Clock {

    private final PieceIndex color;
    private final boolean isBoardReversed;
    private final Label messageLabel;

    private double remainingTime;
    private final double threshold = 10.0;
    private boolean isTicking = false;
    private long lastUpdate = 0;

    private AnimationTimer timer;


    public Clock(StackPane root, PieceIndex color, boolean isBoardReversed) {
        Font courierPrimeBold = Font.loadFont(
                getClass().getResourceAsStream("/fonts/courier-prime.bold.ttf"), 45
        );

        this.isBoardReversed = isBoardReversed;
        this.color = color;

        this.messageLabel = new Label();
        this.messageLabel.setFont(Font.font(courierPrimeBold.getName(), 45));
        StackPane.setAlignment(messageLabel, Pos.CENTER);

        if (!root.getChildren().contains(messageLabel)) {
            root.getChildren().add(messageLabel);
        }
    }

    public void init(double initialTime) {
        this.remainingTime = initialTime;
        updateTimer();
    }

    public void stop() {
        isTicking = false;

        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public boolean hasTimeout() {
        return remainingTime <= 0;
    }

    public boolean isTicking() {
        return isTicking;
    }

    public void pause() {
        isTicking = false;
        updateTimer();
    }

    public void resume() {
        isTicking = true;
        startTimer();
    }

    public void setTime(double time) {
        this.remainingTime = time;
        updateTimer();
    }

    public double getTime() {
        return remainingTime;
    }

    private void startTimer() {
        if (timer == null) {
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (isTicking) {
                        if (lastUpdate > 0) {
                            double delta = (now - lastUpdate) / 1_000_000_000.0;
                            remainingTime = Math.max(0, remainingTime - delta);
                            updateTimer();
                        }
                    }
                    lastUpdate = now;
                }
            };
            timer.start();
        }
    }

    private void updateTimer() {
        String message;

        int min = (int) Math.floor(remainingTime / 60);
        int sec = (int) remainingTime % 60;

        if (remainingTime < threshold) {
            messageLabel.setTextFill(isTicking ? Color.rgb(227,88,105) : Color.GREY);
            int tenths = (int) ((remainingTime - Math.floor(remainingTime)) * 10);
            message = String.format("%02d:%02d.%d", min, sec, tenths);
        } else {
            messageLabel.setTextFill(isTicking ? Color.WHITE : Color.GREY);
            message = String.format("%02d:%02d", min, sec);
        }

        messageLabel.setText(message);
    }
}
