package fr.chessproject.chessfx.view.components;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.game.Game;
import fr.chessproject.chessfx.model.game.GameState;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class EndGamePopup {

    private static BorderPane popupBox;

    private static BorderPane setupPopUp(Pane root, int squareSize) {
        BorderPane popupBox = new BorderPane();

        popupBox.setStyle(
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
        popupBox.setPrefSize(popupWidth, popupHeight);

        popupBox.setTranslateX(root.getLayoutX() + (root.getWidth() - popupWidth) / 2);
        popupBox.setTranslateY(root.getLayoutY() + (root.getHeight() - popupHeight) / 2);

        ChessPopUp.consumeEvents(root);
        root.setPickOnBounds(true);

        return popupBox;
    }

    public static void show(ChessController controller, Pane root, int squareSize) {
        Game game = controller.getGame();
        GameState state = game.getGameState();

        String message = getMessageForState(state);

        Text messageText = new Text(message);
        messageText.setFont(Font.font("open-sans", 20));
        messageText.setFill(Color.rgb(199,198,198));

        Button closeButton = new Button();
        closeButton.setId("closeButton");
        ButtonUtilities.loadButtonIcon(closeButton);

        StackPane topBar = new StackPane(closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);

        StackPane content = new StackPane(messageText);
        StackPane.setAlignment(messageText, Pos.CENTER);

        popupBox = setupPopUp(root, squareSize);
        popupBox.setTop(topBar);
        popupBox.setCenter(content);

        closeButton.setOnAction(e -> {
            e.consume();
            close(root);
        });

        root.getChildren().add(popupBox);
    }

    public static void close(Pane root) {
        if (popupBox != null && root.getChildren().contains(popupBox)) {
            root.getChildren().remove(popupBox);
            root.setPickOnBounds(false);
        }
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
