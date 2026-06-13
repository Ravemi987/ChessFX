package fr.chessproject.chessfx.view.components;

import javafx.scene.paint.Color;

public class Theme {
    private final Color lightSquare;
    private final Color darkSquare;
    private final Color drawingLightSquare;
    private final Color drawingDarkSquare;
    private final Color selectedLightSquare;
    private final Color selectedDarkSquare;
    private final Color getLastMoveLightStartSquare;
    private final Color getLastMoveLightEndSquare;
    private final Color getLastMoveDarkStartSquare;
    private final Color getLastMoveDarkEndSquare;
    private final Color validMoveLightSquare;
    private final Color validMoveDarkSquare;
    private final Color hoverSelectedSquare;
    private final Color hoverLightSquare;
    private final Color hoverDarkSquare;

    public Theme(Color lightSquare, Color darkSquare, Color drawingLightSquare, Color drawingDarkSquare, Color selectedLightSquare, Color selectedDarkSquare, Color getLastMoveLightStartSquare, Color getLastMoveLightEndSquare, Color getLastMoveDarkStartSquare, Color getLastMoveDarkEndSquare, Color validMoveLightSquare, Color validMoveDarkSquare, Color hoverSelectedSquare, Color hoverLightSquare, Color hoverDarkSquare) {
        this.lightSquare = lightSquare;
        this.darkSquare = darkSquare;
        this.drawingLightSquare = drawingLightSquare;
        this.drawingDarkSquare = drawingDarkSquare;
        this.selectedLightSquare = selectedLightSquare;
        this.selectedDarkSquare = selectedDarkSquare;
        this.getLastMoveLightStartSquare = getLastMoveLightStartSquare;
        this.getLastMoveLightEndSquare = getLastMoveLightEndSquare;
        this.getLastMoveDarkStartSquare = getLastMoveDarkStartSquare;
        this.getLastMoveDarkEndSquare = getLastMoveDarkEndSquare;
        this.validMoveLightSquare = validMoveLightSquare;
        this.validMoveDarkSquare = validMoveDarkSquare;
        this.hoverSelectedSquare = hoverSelectedSquare;
        this.hoverLightSquare = hoverLightSquare;
        this.hoverDarkSquare = hoverDarkSquare;
    }

    public Color getLightSquare() {
        return lightSquare;
    }

    public Color getDarkSquare() {
        return darkSquare;
    }

    public Color getDrawingLightSquare() {
        return drawingLightSquare;
    }

    public Color getDrawingDarkSquare() {
        return drawingDarkSquare;
    }

    public Color getValidMoveLightSquare() {
        return validMoveLightSquare;
    }

    public Color getValidMoveDarkSquare() {
        return validMoveDarkSquare;
    }

    public Color getHoverSelectedSquare() {
        return hoverSelectedSquare;
    }

    public Color getHoverLightSquare() {
        return hoverLightSquare;
    }

    public Color getHoverDarkSquare() {
        return hoverDarkSquare;
    }

    public Color getSelectedLightSquare() {
        return selectedLightSquare;
    }

    public Color getSelectedDarkSquare() {
        return selectedDarkSquare;
    }

    public Color getLastMoveLightStartSquare() {
        return getLastMoveLightStartSquare;
    }

    public Color getLastMoveLightEndSquare() {
        return getLastMoveLightEndSquare;
    }

    public Color getLastMoveDarkStartSquare() {
        return getLastMoveDarkStartSquare;
    }

    public Color getLastMoveDarkEndSquare() {
        return getLastMoveDarkEndSquare;
    }
}
