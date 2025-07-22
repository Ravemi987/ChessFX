package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.model.PieceIndex;
import fr.chessproject.chessfx.model.PieceType;
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
    private static final Map<PieceType, Byte> spritesToPieces = new HashMap<>() {{
        put(PieceType.WHITE_QUEEN, PieceIndex.QUEENS.id);
        put(PieceType.WHITE_KNIGHT, PieceIndex.KNIGHTS.id);
        put(PieceType.WHITE_ROOK, PieceIndex.ROOKS.id);
        put(PieceType.WHITE_BISHOP, PieceIndex.BISHOPS.id);
        put(PieceType.BLACK_QUEEN, PieceIndex.QUEENS.id);
        put(PieceType.BLACK_KNIGHT, PieceIndex.KNIGHTS.id);
        put(PieceType.BLACK_ROOK, PieceIndex.ROOKS.id);
        put(PieceType.BLACK_BISHOP, PieceIndex.BISHOPS.id);
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

    private static VBox setupPopUp(double x, double y, boolean isWhite, int iconSize, boolean isBoardReversed) {
        double spacing = 10;
        VBox vbox = new VBox(spacing);

        vbox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0.3, 4, 4);"
        );

        if ((!isWhite && !isBoardReversed) || (isWhite && isBoardReversed)) {
            double totalHeight = iconSize * 3 + spacing * 3;
             y -= totalHeight;
        }

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

    private static PieceType[] getPiecesOrder(boolean isWhite, boolean isBoardReversed) {
        return new PieceType[]{
                isWhite ? (isBoardReversed ? PieceType.WHITE_BISHOP : PieceType.WHITE_QUEEN) : (isBoardReversed ? PieceType.BLACK_QUEEN : PieceType.BLACK_BISHOP),
                isWhite ? (isBoardReversed ? PieceType.WHITE_ROOK : PieceType.WHITE_KNIGHT) : (isBoardReversed ? PieceType.BLACK_KNIGHT : PieceType.BLACK_ROOK),
                isWhite ? (isBoardReversed ? PieceType.WHITE_KNIGHT : PieceType.WHITE_ROOK) : (isBoardReversed ? PieceType.BLACK_ROOK : PieceType.BLACK_KNIGHT),
                isWhite ? (isBoardReversed ? PieceType.WHITE_QUEEN : PieceType.WHITE_BISHOP) : (isBoardReversed ? PieceType.BLACK_BISHOP : PieceType.BLACK_QUEEN)
        };
    }

    public static void showPromotionVBox(Pane root, GameSpritesLoader spritesLoader, byte color, int squareSize,
                                         boolean isBoardReversed, double x, double y, Consumer<Byte> onPieceSelected,
                                         Runnable onCancelled, Runnable suppressNextRightClick) {

        boolean isWhite = color == PieceIndex.WHITE_PIECES.id;
        PieceType[] pieces = getPiecesOrder(isWhite, isBoardReversed);
        int iconSize = squareSize - 2;

        VBox vbox = setupPopUp(x, y, isWhite, iconSize, isBoardReversed);

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

        for (PieceType piece : pieces) {
            ImageView img = new ImageView(spritesLoader.getPieceSprite(piece.id));
            img.setFitWidth(iconSize);
            img.setFitHeight(iconSize);

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
