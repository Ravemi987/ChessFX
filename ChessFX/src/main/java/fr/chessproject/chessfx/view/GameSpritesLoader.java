package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.model.Piece;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameSpritesLoader {
    private final Image piecesImage;
    private int pieceSpriteScale;

    private final Map<Integer, Image> pieceSpritesMap;

    public GameSpritesLoader(int squareSize) {
        piecesImage = loadPiecesSprites();
        pieceSpritesMap = new HashMap<>();
        if (piecesImage != null) {
            pieceSpriteScale = (int) piecesImage.getWidth() / 6;
            for (int i = 0; i < 6; i++) {
                pieceSpritesMap.put(Piece.WHITE_KING + i, setPieceSprite(i, 0, squareSize));
                pieceSpritesMap.put(Piece.BLACK_KING + i, setPieceSprite(i, 1, squareSize));
            }
        }
    }

    private Image loadPiecesSprites() {
        Image piecesImage = null;

        try {
            piecesImage = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("Chess_Pieces_Sprite_Small.png")), 0, 0, true, true);
        } catch (Exception e) {
            System.err.println("Failed to load pieces sprites");
            System.exit(1);
        }
        return piecesImage;
    }

    private Image setPieceSprite(int pieceIndex, int row, int squareSize) {
        Image sprite = new WritableImage(piecesImage.getPixelReader(), pieceIndex * pieceSpriteScale, row * pieceSpriteScale, pieceSpriteScale, pieceSpriteScale);
        ImageView imageView = new ImageView(sprite);
        imageView.setFitWidth(squareSize);
        imageView.setFitHeight(squareSize);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        return imageView.snapshot(params, null);
    }

    public Image getPieceSprite(int piece) {
        return pieceSpritesMap.get(piece);
    }

    public Image getPiecesImage() {
        return piecesImage;
    }
}
