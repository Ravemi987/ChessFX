package fr.chessproject.chessfx.view;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PromotionPopup {
    private static final Map<Byte, Byte> spritesToPieces = new HashMap<>() {{
        put((byte) 2, (byte) 6);
        put((byte) 4, (byte) 3);
        put((byte) 5, (byte) 5);
        put((byte) 3, (byte) 4);
    }};

    private static boolean isAncestorOf(Node parent, Node child) {
        while (child != null) {
            if (child == parent) return true;
            child = child.getParent();
        }
        return false;
    }

    private static void consumeEvents(Pane pane) {
        pane.setOnMousePressed(Event::consume);
        pane.setOnMouseReleased(Event::consume);
        pane.setOnMouseDragged(Event::consume);
        pane.setOnMouseMoved(Event::consume);
    }

    private static VBox setupPopUp(int x, int y) {
        VBox vbox = new VBox(10);

        vbox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 4px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0.3, 4, 4);"
        );
        vbox.setTranslateX(x);
        vbox.setTranslateY(y);

        consumeEvents(vbox);

        return vbox;
    }

    private static void setupIconsEventsFilters(StackPane stack) {
        stack.setOnMouseEntered(e -> stack.setStyle("-fx-background-color: #eeeeee;"));
        stack.setOnMouseExited(e -> stack.setStyle("-fx-background-color: white;"));
        stack.setOnMouseReleased(Event::consume);
        stack.setOnMouseClicked(Event::consume);
    }

    public static void showPromotionVBox(Pane root, GameSpritesLoader spritesLoader, int x, int y,
                                         Consumer<Byte> onPieceSelected, Runnable onCancelled, Runnable suppressNextRightClick) {

        VBox vbox = setupPopUp(x, y);

        EventHandler<MouseEvent> outsideClickHandler = new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                Node target = event.getPickResult().getIntersectedNode();
                if (!isAncestorOf(vbox, target)) {
                    event.consume();
                    if (event.getButton() == MouseButton.SECONDARY) suppressNextRightClick.run();
                    root.getChildren().remove(vbox);
                    root.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
                    onCancelled.run();
                }
            }
        };

        byte[] pieces = {2, 4, 5, 3};

        for (byte piece : pieces) {
            ImageView img = new ImageView(spritesLoader.getPieceSprite(piece));

            StackPane stack = new StackPane(img);
            setupIconsEventsFilters(stack);
            
            stack.setOnMousePressed(e -> {
                e.consume();
                
                root.getChildren().remove(vbox);
                root.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);

                if (e.getButton() == MouseButton.PRIMARY) {
                    onPieceSelected.accept(spritesToPieces.get(piece));
                } else {
                    onCancelled.run();
                }
            });
            VBox.setMargin(stack, javafx.geometry.Insets.EMPTY);
            vbox.getChildren().add(stack);
        }

        root.getChildren().add(vbox);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, outsideClickHandler);
    }
}
