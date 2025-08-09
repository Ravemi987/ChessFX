package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.Game;
import fr.chessproject.chessfx.model.GameState;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class EndGamePopup {

    private static VBox setupPopUp(Pane root, int squareSize) {
        VBox vbox = new VBox(10);

        vbox.setStyle(
                "-fx-background-color: rgb(49, 46, 43);" +
                        "-fx-border-color: rgb(38, 37, 34);" +
                        "-fx-border-width: 4px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0.3, 4, 4);"
        );

        double popupWidth = squareSize * 2.5;
        double popupHeight = squareSize * 3.5;
        vbox.setPrefSize(popupWidth, popupHeight);

        vbox.setTranslateX(root.getLayoutX() + (root.getWidth() - popupWidth) / 2);
        vbox.setTranslateY(root.getLayoutY() + (root.getHeight() - popupHeight) / 2);

        ChessPopUp.consumeEvents(root);
        root.setPickOnBounds(true);

        return vbox;
    }

    public static void show(ChessController controller, Pane root, int squareSize) {
        Game game = controller.getGame();
        GameState state = game.getGameState();
        String message = getMessageForState(state);

        Text messageText = new Text(message);
        messageText.setFont(Font.font("open-sans", 20));
        messageText.setFill(Color.rgb(199,198,198));

        Button closeButton = new Button("x");
        closeButton.setPrefSize(100, 0.5);
        closeButton.setStyle(
                "-fx-background-color: rgb(56,54,52);" +
                "-fx-border-color: rgb(56,54,52);" +
                "-fx-border-width: 4px;" +
                "-fx-border-radius: 8px;" +
                "-fx-padding: 10;" +
                "-fx-font-size: 20;" +
                "-fx-text-fill: white;"
        );

        StackPane topBar = new StackPane(closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);

        StackPane content = new StackPane(messageText);
        StackPane.setAlignment(messageText, Pos.CENTER);

        VBox vbox = setupPopUp(root, squareSize);
        vbox.getChildren().add(topBar);
        vbox.getChildren().add(content);

        EventHandler<MouseEvent> clickHandler = new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                Node target = event.getPickResult().getIntersectedNode();
                if (!ChessPopUp.isAncestorOf(vbox, target)) {
                    event.consume();
                    root.getChildren().remove(vbox);
                    root.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
                    root.setPickOnBounds(false);
                }
            }
        };

        closeButton.setOnAction(e -> {
            e.consume();
            root.getChildren().remove(vbox);
            root.removeEventFilter(MouseEvent.MOUSE_PRESSED, clickHandler);
            root.setPickOnBounds(false);
        });

        root.getChildren().add(vbox);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, clickHandler);
    }

    private static String getMessageForState(GameState state) {
        return switch (state) {
            case WHITE_WON -> "White won by checkmate.";
            case BLACK_WON -> "Black won by checkmate.";
            case STALEMATE -> "Draw by stalemate.";
            case THREEFOLD_REPETITION -> "Draw by threefold repetition.";
            case FIFTY_MOVE_RULE -> "Draw by fifty-move rule.";
            case INSUFFICIENT_MATERIAL -> "Draw by insufficient material.";
            case DRAW_BY_AGREEMENT -> "Draw by mutual agreement.";
            case WHITE_RESIGNED -> "Blacks won by resignation.";
            case BLACK_RESIGNED -> "Whites won by resignation.";
            case WHITE_TIMEOUT -> "Black won by timeout.";
            case BLACK_TIMEOUT -> "White won by timeout.";
            default -> "Game Over";
        };
    }
}