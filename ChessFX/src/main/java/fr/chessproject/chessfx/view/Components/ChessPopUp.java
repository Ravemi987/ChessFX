package fr.chessproject.chessfx.view.Components;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class ChessPopUp {

    public static boolean isAncestorOf(Node parent, Node child) {
        while (child != null) {
            if (child == parent) return true;
            child = child.getParent();
        }
        return false;
    }

    public static void consumeEvents(Pane pane) {
        pane.setOnMousePressed(Event::consume);
        pane.setOnMouseReleased(Event::consume);
        pane.setOnMouseDragged(Event::consume);
        pane.setOnMouseMoved(Event::consume);
    }
}
