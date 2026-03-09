package fr.chessproject.chessfx.view.components;

import javafx.scene.canvas.GraphicsContext;

import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import java.util.Objects;

public class Arrow {
    final int fromSquare;
    final int toSquare;
    final Color color;

    public Arrow(int fromSquare, int toSquare, Color color) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.color = color;
    }

    public void draw(GraphicsContext gc, Point2D start, Point2D end, int squareSize) {
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());

        double angle = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
        double arrowLength = 0.24 * squareSize;
        double arrowAngle = Math.toRadians(40);

        double x1 = end.getX() - arrowLength * Math.cos(angle - arrowAngle);
        double y1 = end.getY() - arrowLength * Math.sin(angle - arrowAngle);
        double x2 = end.getX() - arrowLength * Math.cos(angle + arrowAngle);
        double y2 = end.getY() - arrowLength * Math.sin(angle + arrowAngle);

        gc.strokeLine(end.getX(), end.getY(), x1, y1);
        gc.strokeLine(end.getX(), end.getY(), x2, y2);
    }

    public int getFromSquare() {
        return fromSquare;
    }

    public int getToSquare() {
        return toSquare;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Arrow arrow = (Arrow) o;
        return fromSquare == arrow.fromSquare && toSquare == arrow.toSquare;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromSquare, toSquare);
    }
}
