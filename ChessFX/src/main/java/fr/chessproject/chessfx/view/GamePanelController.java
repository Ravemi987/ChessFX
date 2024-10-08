package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.*;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePanelController {

    // Panes

    @FXML
    public Pane boardPane;
    @FXML
    public Pane boardMaskPane;

    //Canvas

    @FXML
    public Canvas boardCanvas;
    @FXML
    public Canvas coordsCanvas;
    @FXML
    public Canvas piecesCanvas;
    @FXML
    public Canvas draggingCanvas;
    @FXML
    public Canvas coloredSquaresCanvas;
    @FXML
    public Canvas drawingCanvas;

    private ChessController controller;

    private final Map<Byte, Image> resizedPieceSprites = new HashMap<>();

    private static final double TICKS_PER_SECOND = 120;
    private static final double NS_PER_TICK = 1_000_000_000 / TICKS_PER_SECOND;

    private GameSpritesLoader spritesLoader;
    private WritableImage staticBoardImage;
    private WritableImage staticCoordsImage;

    private boolean[] isSquareColored;
    private int selectedPiece;
    private int previousSelectedPiece;
    private int draggedPiece;
    private int clickedPiece;
    private int releasePiece;
    private boolean dragging;
    private int mouseXOnBoard;
    private int mouseYOnBoard;

    public GamePanelController() {
        System.out.println("GamePanelController created");

        isSquareColored = new boolean[64];

        controller = null;
        selectedPiece = -1;
        previousSelectedPiece = -1;
        draggedPiece = -1;
        clickedPiece = -1;
        releasePiece = -1;
        dragging = false;
        mouseXOnBoard = 0;
        mouseYOnBoard = 0;
    }

    @FXML
    public void initialize() {
        System.out.println("GamePanelController initialized");
        boardPane.getProperties().put("controller", this);
    }

    public void init() {
        loadGraphics();
        renderBoard();
        renderCoordinates();
        renderPieces();
        setupMouseEvents();
        startGameLoop();
    }

    private void precalculatePieceSprites() {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        int scaledWidth = (int) (squareSize * 1.05);

        for (byte piece = Piece.WHITE_KING; piece <= Piece.BLACK_PAWN; piece++) {
            ImageView imageView = new ImageView(spritesLoader.getPieceSprite(piece));
            imageView.setFitWidth(scaledWidth);
            imageView.setFitHeight(scaledWidth);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(true);

            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            Image snapshot = imageView.snapshot(params, null);

            resizedPieceSprites.put(piece, snapshot);
        }
    }

    public void setupMouseEvents() {
        boardMaskPane.setOnMousePressed(this::handleMousePressed);
        boardMaskPane.setOnMouseDragged(this::handleMouseDragged);
        boardMaskPane.setOnMouseReleased(this::handleMouseReleased);
        boardMaskPane.setOnMouseMoved(this::handleMouseMoved);
    }

    private void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {

            long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= NS_PER_TICK) {
                    updateGameState();
                    renderDragging();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    private void renderBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());
        //gc.drawImage(staticBoardImage, 0, 0);
        drawBoard(gc);
    }

    private void renderCoordinates() {
        GraphicsContext gc = coordsCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, coordsCanvas.getWidth(), coordsCanvas.getHeight());
        //gc.drawImage(staticCoordsImage, 0, 0);
        drawCoordinates(gc);
    }

    private void renderColoredSquares() {
        GraphicsContext gc = coloredSquaresCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, coloredSquaresCanvas.getWidth(), coloredSquaresCanvas.getHeight());
        showSelectedPiece(gc);
        showLastMove(gc);
        showValidMoves(gc);
    }

    private void renderPieces() {
        GraphicsContext gc = piecesCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, piecesCanvas.getWidth(), piecesCanvas.getHeight());
        drawPieces(gc);
    }

    private void renderDragging() {
        GraphicsContext gc = draggingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, draggingCanvas.getWidth(), draggingCanvas.getHeight());
        if (dragging) {
            showHover(gc);
            draggerUpdateBlit(gc);
        }
    }

    private void render() {
        renderColoredSquares();
        renderPieces();
        renderDragging();
    }

    private void updateGameState() {
    }

    public void loadGraphics() {
        int squareSize = (int) ((boardCanvas.getWidth()) / 8);
        spritesLoader = new GameSpritesLoader(squareSize);
//        initializeStaticBoardImage();
//        initializeStaticCoordsImage();
        //precalculatePieceSprites();
    }

    private void showHover(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        byte hoveredSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        if (selectedPiece != -1) {
            int row = 7 - hoveredSquare / 8;
            int col = hoveredSquare % 8;
            Color c;

            if (hoveredSquare == selectedPiece) {
                c = Color.rgb(242, 234, 183);
            } else if ((row + col) % 2 == 0) {
                c = Color.rgb(250, 242, 229);
            } else {
                c = Color.rgb(229, 213, 201);
            }

            double width = 5.7;
            gc.setStroke(c);
            gc.setLineWidth(width);
            gc.setLineCap(StrokeLineCap.ROUND);

            double offset = width / 2.0;
            gc.strokeRect(col * squareSize + offset,row  * squareSize + offset, squareSize - width, squareSize - width);
        }
    }

    private void showValidMoves(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        if (selectedPiece != -1) {
            MoveList validMoves = controller.getGame().getValidMoves();
            for (int i = 0; i < validMoves.getMvCount(); i++) {
                Move mv = validMoves.getMove(i);
                if (mv.getFrom() == selectedPiece) {
                    int row = 7 - mv.getTo() / 8;
                    int col = mv.getTo() % 8;
                    gc.setFill(((row + col) % 2 == 0 ) ? Color.rgb(235, 121, 99) : Color.rgb(225, 105, 84));
                    gc.fillRect(col * squareSize,row  * squareSize, squareSize, squareSize);
                }
            }
        }
    }

    private void showLastMove(GraphicsContext gc) {
        Move lastMove = controller.getGame().getLastMove();
        if (lastMove != null) {
            int squareSize = (int) (boardCanvas.getWidth() / 8);
            int fromRow = 7 - lastMove.getFrom() / 8, toRow = 7 - lastMove.getTo() / 8;
            int fromCol = lastMove.getFrom() % 8, toCol = lastMove.getTo() % 8;
            gc.setFill(((fromRow + fromCol) % 2 == 0 ) ? Color.rgb(246, 235, 114) : Color.rgb(220, 195, 75));
            gc.fillRect(fromCol * squareSize,fromRow  * squareSize, squareSize, squareSize);
            gc.setFill(((toRow + toCol) % 2 == 0 ) ? Color.rgb(246, 235, 114) : Color.rgb(220, 195, 75));
            gc.fillRect(toCol * squareSize,toRow  * squareSize, squareSize, squareSize);
        }
    }

    private void showSelectedPiece(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        if (selectedPiece != -1) {
            int row = 7 - selectedPiece / 8;
            int col = selectedPiece % 8;
            gc.setFill(((row + col) % 2 == 0 ) ? Color.rgb(246, 235, 114) : Color.rgb(220, 195, 75));
            gc.fillRect(col * squareSize,row  * squareSize, squareSize, squareSize);
        }
    }

    private void drawPiece(GraphicsContext gc, byte piece, int x, int y) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        gc.drawImage(spritesLoader.getPieceSprite(piece), x, y, squareSize, squareSize);
    }

//    private void drawSelectedPiece(GraphicsContext gc, byte piece, int x, int y) {
//        Image image = resizedPieceSprites.get(piece);
//        if (image != null) {
//            gc.drawImage(image, x, y);
//        }
//    }

    private void drawSelectedPiece(GraphicsContext gc, byte piece, int x, int y) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        int scaledWidth = (int) (squareSize * 1.05);
        int scaledHeight = (int) (squareSize * 1.05);

        gc.drawImage(spritesLoader.getPieceSprite(piece), x, y, scaledWidth, scaledHeight);
    }

    private void drawPieces(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        Position position = controller.getGame().getPosition();

        for (int i = 0; i < 64; i++) {
            byte piece = position.pieceOnSquare((byte)i);
            if (piece != Piece.NONE && i != draggedPiece) {
                int row = 7 - (i / 8);
                int col = (i % 8);
                drawPiece(gc, piece, col * squareSize, row * squareSize);
            }
        }
    }

    private void draggerUpdateBlit(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        Position position = controller.getGame().getPosition();
        drawSelectedPiece(gc, position.pieceOnSquare((byte) selectedPiece),
                (int) (mouseXOnBoard - (1.05*squareSize) / 2),
                (int) (mouseYOnBoard - (1.05*squareSize) / 2));
    }

    private void drawCoordinates(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        double fontSize = 24;

        gc.setFont(new Font("Arial", fontSize));

        for (int row = 0; row < 8; row++) {
            gc.setFill(((row % 2) == 0) ? Color.rgb(181, 136, 99) : Color.rgb(240, 217, 181));
            gc.fillText(String.valueOf(8 - row), 10, row * squareSize + fontSize);
        }

        for (int col = 0; col < 8; col++) {
            gc.setFill(((col % 2) == 0) ? Color.rgb(240, 217, 181) : Color.rgb(181, 136, 99));
            gc.fillText(String.valueOf((char) ('a' + col)), col * squareSize + (squareSize - fontSize), boardCanvas.getHeight() - (fontSize / 2));
        }
    }

    private void drawBoard(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                gc.setFill(((row + col) % 2 == 0 ) ? Color.rgb(240, 217, 181) : Color.rgb(181, 136, 99));
                gc.fillRect(col*squareSize, row*squareSize, squareSize, squareSize);
            }
        }
    }

    private void drawBitboard(GraphicsContext gc, long bitboard) {
        double squareSize = boardCanvas.getWidth() / 8;
        gc.setFont(new Font("Arial", squareSize / 2));

        for (int i = 0; i < 64; i++) {
            char bit = ((bitboard >> i) & 1) == 1 ? '1' : '0';

            gc.setFill((bit == '0') ? Color.rgb(70, 57, 57) : Color.rgb(194, 181, 45));

            Text text = new Text(String.valueOf(bit));
            text.setFont(gc.getFont());
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();

            int row = 7 - (i / 8);
            int col = (i % 8);
            int x = (int) (col * squareSize + squareSize / 2 - textWidth / 2);
            int y = (int) (row * squareSize + squareSize / 2 + textHeight / 4);
            gc.fillText(String.valueOf(bit), x, y);
        }
    }

    private void initializeStaticBoardImage() {
        int width = (int) boardCanvas.getWidth();
        int height = (int) boardCanvas.getHeight();

        staticBoardImage = new WritableImage(width, height);
        Canvas tempCanvas = new Canvas(width, height);
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();

        gc.setImageSmoothing(true);

        drawBoard(gc);

        tempCanvas.snapshot(null, staticBoardImage);
    }

    private void initializeStaticCoordsImage() {
        int width = (int) coordsCanvas.getWidth();
        int height = (int) coordsCanvas.getHeight();

        staticCoordsImage = new WritableImage(width, height);
        Canvas tempCanvas = new Canvas(width, height);
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();

        gc.setImageSmoothing(true);

        drawCoordinates(gc);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        tempCanvas.snapshot(params, staticCoordsImage);
    }

    public void updateMousePos(double absX, double abY) {
        int x = (int) absX;
        int y = (int) abY;

        if (inBoardRect(x)) {
            mouseXOnBoard = x;
        }
        if (inBoardRect(y)) {
            mouseYOnBoard = y;
        }
    }

    public void dragPiece(int piece) {
        draggedPiece = piece;
        dragging = true;
    }

    public void undragPiece() {
        draggedPiece = -1;
        dragging = false;
    }

    public void selectPiece(int piece) {
        selectedPiece = piece;
    }

    public void unselectPiece() {
        selectedPiece = -1;
    }

    public boolean inBoardRect(int coord) {
        return  0 <= coord && coord <= (int) boardCanvas.getWidth();
    }

    public byte getSquareFromPos(int x, int y) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        int col = Math.min(7, Math.max(0, x / squareSize));
        int row = Math.min(7, Math.max(0, 7 - (y / squareSize)));
        int squareIndex = col % 8 + row * 8;
        return (byte) squareIndex;
    }

    private void clearDrawingCanvas() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        isSquareColored = new boolean[64];
    }

    private void drawOnCanvas() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        int squareSize = (int) (drawingCanvas.getWidth() / 8);

        int square = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        isSquareColored[square] = !isSquareColored[square];
        int row = 7 - square / 8;
        int col = square % 8;

        if (isSquareColored[square]) {
            gc.setFill((row + col) % 2 == 0 ? Color.rgb(113, 217, 100) : Color.rgb(50, 200, 100));
        } else {
            gc.setFill(((row + col) % 2 == 0 ) ? Color.rgb(240, 217, 181) : Color.rgb(181, 136, 99));
        }
        gc.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
    }

    private void resetSelection() {
        unselectPiece();
        undragPiece();
        previousSelectedPiece = -1;
    }

    @FXML
    private void handleMouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            handleLeftMouseReleased();
        } else {
            handleRightMouseReleased();
        }
    }

    private void handleLeftMouseReleased() {
        int sq = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        Position pos = controller.getGame().getPosition();
        if (Square.isOccupied((byte) sq, controller.getGame().getPosition().getOccupied())) {
            releasePiece = sq;
        } else {
            releasePiece = -1;
        }

        if (previousSelectedPiece != -1 && releasePiece == previousSelectedPiece && clickedPiece == previousSelectedPiece) {
            unselectPiece();
        } else if (selectedPiece != -1) {
            Move mv = new Move((byte) selectedPiece, (byte) sq, pos.pieceBitboardOnSquare((byte) selectedPiece), pos.pieceColorOnSquare((byte) selectedPiece));
            Move validMove = controller.getGame().checkMove(mv);
            if (validMove != null) {
                controller.getGame().playMove(validMove);
                unselectPiece();
            }
        }

        undragPiece();
        previousSelectedPiece = selectedPiece;

        render();
    }

    private void handleRightMouseReleased() {
        if (dragging || selectedPiece != -1) {
            resetSelection();
            render();
        } else {
            drawOnCanvas();
        }
    }

    @FXML
    private void handleMousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) {
            return;
        }

        int clickedSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        Position pos = controller.getGame().getPosition();
        if (Square.isOccupied((byte) clickedSquare, pos.getOccupied())) {
            if (selectedPiece != -1 && pos.pieceColorOnSquare((byte) clickedSquare) != pos.pieceColorOnSquare((byte) selectedPiece)) {
                Move mv = new Move((byte) selectedPiece, (byte) clickedSquare, pos.pieceBitboardOnSquare((byte) selectedPiece), pos.pieceColorOnSquare((byte) selectedPiece));
                Move validMove = controller.getGame().checkMove(mv);
                if (validMove == null) {
                    clickedPiece = clickedSquare;
                    dragPiece(clickedPiece);
                    selectPiece(clickedPiece);
                } else {
                    controller.getGame().playMove(validMove);
                    unselectPiece();
                }
            } else {
                clickedPiece = clickedSquare;
                dragPiece(clickedPiece);
                selectPiece(clickedPiece);
            }
        } else {
            if (selectedPiece != -1) {
                Move mv = new Move((byte) selectedPiece, (byte) clickedSquare, pos.pieceBitboardOnSquare((byte) selectedPiece), pos.pieceColorOnSquare((byte) selectedPiece));
                Move validMove = controller.getGame().checkMove(mv);
                if (validMove == null) {
                    unselectPiece();
                } else {
                    controller.getGame().playMove(validMove);
                    unselectPiece();
                }
            } else {
                unselectPiece();
            }
        }

        clearDrawingCanvas();
        render();
    }

    @FXML
    private void handleMouseDragged(MouseEvent mouseEvent) {
        updateMousePos(mouseEvent.getX(), mouseEvent.getY());
    }

    @FXML
    public void handleMouseMoved(MouseEvent mouseEvent) {
        updateMousePos(mouseEvent.getX(), mouseEvent.getY());
    }

    public void setMainController(ChessController controller) {
        this.controller = controller;
    }
}
