package fr.chessproject.chessfx.view.manager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

public class AnimationManager {
    private static final double INSET = 0.05;

    public static void playCheckAnimation(int row, int col, Canvas canvas) {
        int squareSize = (int) (canvas.getWidth() / 8);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double centerX = col * squareSize + squareSize / 2.0;
        double centerY = row * squareSize + squareSize / 2.0;
        double radius = squareSize * 0.48;

        RadialGradient gradient = new RadialGradient(
                0, 0, centerX, centerY, radius,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 0, 0, 1)),
                new Stop(0.8, Color.rgb(255, 0, 0, 0.4)),
                new Stop(1.0, Color.rgb(255, 0, 0, 0))
        );

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    gc.setFill(gradient);
                    gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                }),
                new KeyFrame(Duration.seconds(0.2), e -> gc.clearRect(
                        col * squareSize + INSET, row * squareSize + INSET,
                        squareSize - 2 * INSET, squareSize - 2 * INSET
                ))
        );

        timeline.setCycleCount(1);
        timeline.play();
    }
}
