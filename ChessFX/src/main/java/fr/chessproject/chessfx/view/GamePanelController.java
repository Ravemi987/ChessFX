package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.helpers.Constants;
import fr.chessproject.chessfx.model.board.*;
import fr.chessproject.chessfx.model.game.Game;
import fr.chessproject.chessfx.model.game.MoveListener;
import fr.chessproject.chessfx.view.components.*;
import fr.chessproject.chessfx.view.animation.AnimationManager;
import fr.chessproject.chessfx.view.animation.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class GamePanelController implements MoveListener {

    // Panes

    @FXML
    public StackPane boardPane;
    @FXML
    public Pane boardMaskPane;
    @FXML
    public Pane popupLayer;
    @FXML
    private Pane boardContainer;

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
    @FXML
    public Canvas bitboardCanvas;
    @FXML
    public Canvas arrowsCanvas;

    private static final double TICKS_PER_SECOND = 120;
    private static final double NS_PER_TICK = 1_000_000_000 / TICKS_PER_SECOND;
    private final DoubleProperty boardSize = new SimpleDoubleProperty(0);
    private final DoubleProperty squareSize = new SimpleDoubleProperty(0);

    private Theme theme;
    private ChessController controller;
    private GameSpritesLoader spritesLoader;

    private boolean[] isSquareColored;
    private int selectedPiece;
    private int previousSelectedPiece;
    private int draggedPiece;
    private int clickedPiece;
    private int releasePiece;
    private boolean dragging;
    private int mouseXOnBoard;
    private int mouseYOnBoard;
    private boolean suppressRightClick;
    private boolean isBoardReversed;

    private MouseEvent currentMouseEvent;
    private int arrowStartSquare;
    private final List<Arrow> arrows = new ArrayList<>();

    public GamePanelController() {
        isSquareColored = new boolean[64];
        controller = null;
        theme = null;
        selectedPiece = -1;
        previousSelectedPiece = -1;
        draggedPiece = -1;
        clickedPiece = -1;
        releasePiece = -1;
        dragging = false;
        mouseXOnBoard = 0;
        mouseYOnBoard = 0;
        arrowStartSquare = -1;
        suppressRightClick = false;
        isBoardReversed = false;
    }

    @FXML
    public void initialize() {
        boardPane.getProperties().put("controller", this);
        setBoardSize();
    }

    public void init() {
        preloadSprites();
        renderBoard();
        renderCoordinates();
        renderPieces();
        //drawBitboard(bitboardCanvas.getGraphicsContext2D(), controller.getDebugBitboard());
        setupMouseEvents();
        startGameLoop();
    }

    private void render() {
        renderColoredSquares();
        renderPieces();
        renderDragging();
        //drawBitboard(bitboardCanvas.getGraphicsContext2D(), controller.getDebugBitboard());
    }

    public Pane getPopupLayer() {
        return popupLayer;
    }

    public Pane getBoardMaskPane() {
        return boardMaskPane;
    }

    public void refreshBoard() {
        render();
    }

    public void setupMouseEvents() {
        boardMaskPane.setOnMousePressed(this::handleMousePressed);
        boardMaskPane.setOnMouseDragged(this::handleMouseDragged);
        boardMaskPane.setOnMouseReleased(this::handleMouseReleased);
        boardMaskPane.setOnMouseMoved(this::handleMouseMoved);
    }

    public void suppressNextRightClick() {
        suppressRightClick = true;
    }

    private int getRow(int square) {
        int row = square / 8;
        return isBoardReversed ? row : 7 - row;
    }

    private int getCol(int square) {
        int col = square % 8;
        return isBoardReversed ? 7 - col : col;
    }

    private void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {

            long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= NS_PER_TICK) {
                    renderDragging();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    private void setBoardSize() {
        boardPane.setPrefSize(Constants.BOARD_SIZE, Constants.BOARD_SIZE);
        boardPane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        boardSize.bind(Bindings.max(
                Constants.BOARD_SIZE,
                Bindings.min(
                        boardPane.widthProperty().subtract(32),
                        boardPane.heightProperty().subtract(32)
                )
        ));

        squareSize.bind(boardSize.divide(8.0));

        boardContainer.prefWidthProperty().bind(boardSize);
        boardContainer.prefHeightProperty().bind(boardSize);

        List<Canvas> canvases = List.of(boardCanvas, coordsCanvas, piecesCanvas,
                draggingCanvas, coloredSquaresCanvas, drawingCanvas, arrowsCanvas, bitboardCanvas);

        for (Canvas canvas : canvases) {
            canvas.widthProperty().bind(boardSize);
            canvas.heightProperty().bind(boardSize);
        }

        boardMaskPane.prefWidthProperty().bind(boardSize);
        boardMaskPane.prefHeightProperty().bind(boardSize);
        popupLayer.prefWidthProperty().bind(boardSize);
        popupLayer.prefHeightProperty().bind(boardSize);

        boardSize.addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                preloadSprites();
                renderBoard();
                renderCoordinates();
                render();
            }
        });

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(boardContainer.prefWidthProperty());
        clip.heightProperty().bind(boardContainer.prefHeightProperty());
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        boardContainer.setClip(clip);
    }

    @Override
    public void onMovePlayed(Move move) {
        if (move.isCapture()) SoundManager.playCaptureSound();
        else SoundManager.playMoveSound();
    }

    @Override
    public void onGameOver() {
        setInteractionsEnabled(false);
        EndGamePopup.show(controller, popupLayer, (int) (boardCanvas.getWidth() / 8));
    }

    @Override
    public void onGameStarted() {
        setInteractionsEnabled(true);
    }

    public void flipBoard() {
        isBoardReversed = !isBoardReversed;
        renderCoordinates();
        renderColoredSquares();
        renderPieces();
        renderDragging();
    }

    public void setInteractionsEnabled(boolean enabled) {
        boardMaskPane.setDisable(!enabled);
    }

    public void playMove(Move mv) {
        Game game = controller.getGame();
        game.playMove(mv);
        if (mv.isCapture()) SoundManager.playCaptureSound();
        else SoundManager.playMoveSound();

        if (game.cannotPlay()) {
            onGameOver();
        }
    }

    public void handleInvalidMove(Move mv) {
        Position pos = controller.getGame().getPosition();
        if ((pos.isInCheck() || pos.isPinned((byte) selectedPiece)) &&
                pos.isFriendly((byte) selectedPiece) && (mv.getFrom() != mv.getTo())) {
            SoundManager.playInvalidMoveSound();
            AnimationManager.playCheckAnimation(
                    getRow(pos.getKingSquare(pos.getFriendlyColor())),
                    getCol(pos.getKingSquare(pos.getFriendlyColor())), coloredSquaresCanvas
            );
        }
    }

    private void renderBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());
        drawBoard(gc);
    }

    private void renderCoordinates() {
        GraphicsContext gc = coordsCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, coordsCanvas.getWidth(), coordsCanvas.getHeight());
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

    public void preloadSprites() {
        spritesLoader = new GameSpritesLoader((int)squareSize.get());
    }

    private void showHover(GraphicsContext gc) {
        int size = (int) squareSize.get();
        byte hoveredSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        if (selectedPiece != -1) {
            int row = getRow(hoveredSquare);
            int col = getCol(hoveredSquare);
            Color c;

            if (hoveredSquare == selectedPiece) {
                c = theme.getHoverSelectedSquare();
            } else if ((row + col) % 2 == 0) {
                c = theme.getHoverLightSquare();
            } else {
                c = theme.getHoverDarkSquare();
            }

            double width = 5.7;
            gc.setStroke(c);
            gc.setLineWidth(width);
            gc.setLineCap(StrokeLineCap.ROUND);

            double offset = width / 2.0;
            gc.strokeRect(col * size + offset,row  * size + offset, size - width, size - width);
        }
    }

    private void showValidMoves(GraphicsContext gc) {
        int size = (int) squareSize.get();
        if (selectedPiece != -1) {
            MoveList validMoves = controller.getGame().getValidMoves();
            for (int i = 0; i < validMoves.getMvCount(); i++) {
                Move mv = validMoves.getMove(i);
                if (mv.getFrom() == selectedPiece) {
                    int row = getRow(mv.getTo());
                    int col = getCol(mv.getTo());
                    gc.setFill(((row + col) % 2 == 0 ) ? theme.getValidMoveLightSquare() : theme.getValidMoveDarkSquare());
                    gc.fillRect(col * size,row  * size, size, size);
                }
            }
        }
    }

    private void showLastMove(GraphicsContext gc) {
        Move lastMove = controller.getGame().getLastMove();
        if (lastMove != null) {
            int size = (int) squareSize.get();
            int fromRow = getRow(lastMove.getFrom()), toRow = getRow(lastMove.getTo());
            int fromCol = getCol(lastMove.getFrom()), toCol = getCol(lastMove.getTo());
            gc.setFill(((fromRow + fromCol) % 2 == 0 ) ? theme.getLastMoveLightStartSquare() : theme.getLastMoveDarkStartSquare());
            gc.fillRect(fromCol * size,fromRow  * size, size, size);
            gc.setFill(((toRow + toCol) % 2 == 0 ) ? theme.getLastMoveLightEndSquare() : theme.getLastMoveDarkEndSquare());
            gc.fillRect(toCol * size,toRow  * size, size, size);
        }
    }

    private void showSelectedPiece(GraphicsContext gc) {
        int size = (int) squareSize.get();
        if (selectedPiece != -1) {
            int row = getRow(selectedPiece);
            int col = getCol(selectedPiece);
            gc.setFill(((row + col) % 2 == 0 ) ? theme.getSelectedLightSquare() : theme.getSelectedDarkSquare());
            gc.fillRect(col * size,row  * size, size, size);
        }
    }

    private void drawPiece(GraphicsContext gc, byte piece, int x, int y) {
        int size = (int) squareSize.get();
        gc.drawImage(spritesLoader.getPieceSprite(piece), x, y, size, size);
    }

    private void drawSelectedPiece(GraphicsContext gc, byte piece, int x, int y) {
        int size = (int) squareSize.get();
        int scaledWidth = (int) (size * 1.05);
        int scaledHeight = (int) (size * 1.05);

        gc.drawImage(spritesLoader.getPieceSprite(piece), x, y, scaledWidth, scaledHeight);
    }

    private void drawPieces(GraphicsContext gc) {
        int size = (int) squareSize.get();
        Position position = controller.getGame().getPosition();

        for (int i = 0; i < 64; i++) {
            PieceType piece = position.pieceOnSquare((byte)i);
            if (piece != PieceType.NONE && i != draggedPiece) {
                int row = getRow(i);
                int col = getCol(i);
                drawPiece(gc, piece.id, col * size, row * size);
            }
        }
    }

    private void draggerUpdateBlit(GraphicsContext gc) {
        int size = (int) squareSize.get();
        Position position = controller.getGame().getPosition();
        drawSelectedPiece(gc, position.pieceOnSquare((byte) selectedPiece).id,
                (int) (mouseXOnBoard - (1.05*size) / 2),
                (int) (mouseYOnBoard - (1.05*size) / 2));
    }

    private void drawCoordinates(GraphicsContext gc) {
        int size = (int) squareSize.get();
        double fontSize = 24;

        gc.setFont(new Font("Arial", fontSize));

        for (int row = 0; row < 8; row++) {
            gc.setFill(((row % 2) == 0) ? theme.getDarkSquare() : theme.getLightSquare());
            gc.fillText(String.valueOf(isBoardReversed ? row + 1 : 8 - row), 10, row * size + fontSize);
        }

        for (int col = 0; col < 8; col++) {
            gc.setFill(((col % 2) == 0) ? theme.getLightSquare() : theme.getDarkSquare());
            gc.fillText(String.valueOf(isBoardReversed ? (char) ('h' - col) : (char) ('a' + col)),
                    col * size + (size - fontSize), boardCanvas.getHeight() - (fontSize / 2));
        }
    }

    private void drawBoard(GraphicsContext gc) {
        int size = (int) squareSize.get();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                gc.setFill(((row + col) % 2 == 0 ) ? theme.getLightSquare() : theme.getDarkSquare());
                gc.fillRect(col * size, row * size, size, size);
            }
        }
    }

    private void drawBitboard(GraphicsContext gc, Supplier<Long> bitboardSupplier) {
        long bitboard = bitboardSupplier.get();
        gc.clearRect(0, 0, bitboardCanvas.getWidth(), bitboardCanvas.getHeight());

        double size = squareSize.get();
        gc.setFont(new Font("Arial",size / 2));
        double alpha = 0.8;

        for (int i = 0; i < 64; i++) {
            char bit = ((bitboard >>> i) & 1) == 1 ? '1' : '0';

            Text text = new Text(String.valueOf(bit));
            text.setFont(gc.getFont());
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();

            int row = getRow(i);
            int col = getCol(i);

            int x = (int) (col * size + size / 2 - textWidth / 2);
            int y = (int) (row * size + size / 2 + textHeight / 4);

            if (bit == '0') {
                gc.setFill((row + col) % 2 == 0 ? Color.rgb(89, 171, 221, alpha) : Color.rgb(62, 144, 195, alpha));
                gc.fillRect(col*size, row*size, size, size);
            } else {
                gc.setFill((row + col) % 2 == 0 ?  Color.rgb(234,89,102, alpha) : Color.rgb(201,51,69, alpha));
                gc.fillRect(col*size, row*size, size, size);
            }

            gc.setFill(Color.rgb(255, 255, 255, alpha));
            gc.fillText(String.valueOf(bit), x, y);
        }
    }

    public void updateMousePos(MouseEvent mouseEvent ) {
        Point2D coords = boardCanvas.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        int x = (int) coords.getX();
        int y = (int) coords.getY();

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
        int size = (int) squareSize.get();
        int col = Math.min(7, Math.max(0, isBoardReversed ? 7 - (x / size) : x / size));
        int row = Math.min(7, Math.max(0, isBoardReversed ? y / size : 7 - (y / size)));
        int squareIndex = col % 8 + row * 8;
        return (byte) squareIndex;
    }

    private void clearDrawingCanvas() {
        GraphicsContext drawingGC = drawingCanvas.getGraphicsContext2D();
        drawingGC.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        isSquareColored = new boolean[64];

        GraphicsContext arrowGC = arrowsCanvas.getGraphicsContext2D();
        arrowGC.clearRect(0, 0, arrowsCanvas.getWidth(), arrowsCanvas.getHeight());
        arrows.clear();
    }

    private void drawHighlight() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        int size = (int) squareSize.get();

        int square = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        isSquareColored[square] = !isSquareColored[square];
        int row = getRow(square);
        int col = getCol(square);

        if (isSquareColored[square]) {
            gc.setFill((row + col) % 2 == 0 ? theme.getDrawingLightSquare() : theme.getDrawingDarkSquare());
            gc.fillRect(col * size, row * size, size, size);
        } else {
            double inset = 0.05;
            gc.clearRect(col * size + inset, row * size + inset, size - 2 * inset, size - 2 * inset);
        }
    }

    private Point2D getSquareCenter(int square, int squareSize) {
        int row = getRow(square);
        int col = getCol(square);
        double x = col * squareSize + squareSize / 2.0;
        double y = row * squareSize + squareSize / 2.0;
        return new Point2D(x, y);
    }

    private Color getColorFromModifiers() {
        boolean ctrl = currentMouseEvent.isControlDown();
        boolean alt = currentMouseEvent.isAltDown();
        boolean shift = currentMouseEvent.isShiftDown();

        if (ctrl) return Color.rgb(0, 128, 255, 0.6);
        if (alt) return Color.rgb(217, 18, 77, 0.6);
        if (shift) return Color.rgb(255, 165, 0, 0.6);
        return Color.rgb(105, 66, 220, 0.6);
    }

    private void renderArrow(Arrow arrow) {
        GraphicsContext gc = arrowsCanvas.getGraphicsContext2D();
        int size = (int) squareSize.get();

        Point2D start = getSquareCenter(arrow.fromSquare(), size);
        Point2D end = getSquareCenter(arrow.toSquare(), size);

        gc.setStroke(arrow.color());
        gc.setLineWidth(0.14 * size);
        gc.setLineCap(StrokeLineCap.ROUND);

        arrow.draw(gc, start, end, size);
    }

    private void resetSelection() {
        unselectPiece();
        undragPiece();
        previousSelectedPiece = -1;
    }

    private void handleMoveCreation(Position pos, byte selectedPiece, byte squarePos, byte piece, byte color,
                                    Consumer<Move> onMoveReady) {
        if (controller.getGame().hasTimeout()) return;

        int promotionRow = color == PieceIndex.WHITE_PIECES.id ? 7 : 0;
        boolean isPawnMoving = piece == PieceIndex.PAWNS.id;
        boolean hasToPlay = (promotionRow == 7 && pos.isWhiteSideToPlay) || (promotionRow == 0 && !pos.isWhiteSideToPlay);

        Move mv = new Move(selectedPiece, squarePos, piece, color);
        int size = (int) squareSize.get();
        int row = getRow(mv.getTo());
        int col = getCol(mv.getTo());

        if (mv.getTo() / 8 == promotionRow && isPawnMoving && hasToPlay) {
            mv.setPromoted((byte) 6);
            if (!controller.getGame().isLegal(mv)) return;
            PromotionPopup.showPromotionVBox(boardMaskPane, spritesLoader, color, size, isBoardReversed,
                    col * size, row * size, promotedPiece -> {
                mv.setPromoted(promotedPiece);
                onMoveReady.accept(mv);
                render();
            }, () -> {
                resetSelection();
                render();
            }, this::suppressNextRightClick);
        } else {
            onMoveReady.accept(mv);
        }
    }

    @FXML
    private void handleMouseReleased(MouseEvent mouseEvent) {
        currentMouseEvent = mouseEvent;
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
            handleMoveCreation(pos, (byte) selectedPiece, (byte) sq, pos.pieceBitboardOnSquare((byte) selectedPiece), pos.pieceColorOnSquare((byte) selectedPiece), mv -> {
                Move move = controller.getGame().checkMove(mv);
                if (move != null) {
                    playMove(move);
                    unselectPiece();
                } else {
                    handleInvalidMove(mv);
                }
            });
        }

        undragPiece();
        previousSelectedPiece = selectedPiece;

        render();
    }

    private void handleRightMouseReleased() {
        if (suppressRightClick) {
            suppressRightClick = false;
            return;
        }

        if (dragging || selectedPiece != -1) {
            resetSelection();
            render();
            return;
        }

        int arrowEndSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        Color color = getColorFromModifiers();
        Arrow arrow = new Arrow(arrowStartSquare, arrowEndSquare, color);

        if ((arrowEndSquare != arrowStartSquare) && !arrows.contains(arrow)) {
            arrows.add(arrow);
            renderArrow(arrow);
            arrowStartSquare = -1;
        } else {
            drawHighlight();
        }
    }

    @FXML
    private void handleMousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) {
            arrowStartSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
            return;
        }

        int clickedSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        Position pos = controller.getGame().getPosition();
        if (Square.isOccupied((byte) clickedSquare, pos.getOccupied())) {
            if (selectedPiece != -1 && pos.pieceColorOnSquare((byte) clickedSquare) != pos.pieceColorOnSquare((byte) selectedPiece)) {
                handleMoveCreation(pos, (byte) selectedPiece, (byte) clickedSquare, pos.pieceBitboardOnSquare((byte) selectedPiece), pos.pieceColorOnSquare((byte) selectedPiece), mv -> {
                    Move validMove = controller.getGame().checkMove(mv);
                    if (validMove == null) {
                        clickedPiece = clickedSquare;
                        dragPiece(clickedPiece);
                        selectPiece(clickedPiece);
                    } else {
                        playMove(validMove);
                        unselectPiece();
                    }
                });
            } else {
                clickedPiece = clickedSquare;
                dragPiece(clickedPiece);
                selectPiece(clickedPiece);
            }
        } else {
            if (selectedPiece != -1) {
                handleMoveCreation(pos, (byte) selectedPiece, (byte) clickedSquare, pos.pieceBitboardOnSquare((byte) selectedPiece), pos.pieceColorOnSquare((byte) selectedPiece), mv -> {
                    Move validMove = controller.getGame().checkMove(mv);
                    if (validMove == null) {
                        unselectPiece();
                    } else {
                        playMove(validMove);
                        unselectPiece();
                    }
                });
            } else {
                unselectPiece();
            }
        }

        clearDrawingCanvas();
        render();
    }

    @FXML
    private void handleMouseDragged(MouseEvent mouseEvent) {
        updateMousePos(mouseEvent);
    }

    @FXML
    public void handleMouseMoved(MouseEvent mouseEvent) {
        updateMousePos(mouseEvent);
    }

    public void setChessController(ChessController controller, Theme theme) {
        this.controller = controller;
        this.theme = theme;
        controller.getGame().addMoveListener(this);
    }
}
