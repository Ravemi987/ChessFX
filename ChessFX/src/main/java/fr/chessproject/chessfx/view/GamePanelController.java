package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.AnimationManager;
import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.controller.SoundManager;
import fr.chessproject.chessfx.model.*;

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
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GamePanelController implements MoveListener {

    // Panes

    @FXML
    public StackPane boardPane;
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
    @FXML
    public Canvas bitboardCanvas;
    @FXML
    public Canvas arrowsCanvas;

    private ChessController controller;

    private static final double TICKS_PER_SECOND = 120;
    private static final double NS_PER_TICK = 1_000_000_000 / TICKS_PER_SECOND;
    private final double SCREEN_SIZE = Screen.getPrimary().getVisualBounds().getHeight();

    private Theme theme;
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
        //System.out.println("GamePanelController created");
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
        //System.out.println("GamePanelController initialized");
        boardPane.getProperties().put("controller", this);
    }

    @Override
    public void onMovePlayed(Move move, Position before, Position after) {
        if (move.isCapture()) SoundManager.playCaptureSound();
        else SoundManager.playMoveSound();
    }

    public void playMove(Move mv) {
        controller.getGame().playMoveOut(mv);

        if (mv.isCapture()) SoundManager.playCaptureSound();
        else SoundManager.playMoveSound();
    }

    public void handleInvalidMoveAnimation(Move mv) {
        Position pos = controller.getGame().getPosition();
        if ((pos.isInCheck() || pos.isPinned((byte) selectedPiece)) &&
                pos.isFriendly((byte) selectedPiece) && (mv.getFrom() != mv.getTo())) {
            byte kingSquare = pos.getKingSquare(pos.getFriendlyColor());
            AnimationManager.playCheckAnimation(getRow(kingSquare), getCol(kingSquare), coloredSquaresCanvas);
            SoundManager.playInvalidMoveSound();
        }
    }

    private void setPaneSize(Pane pane, int size) {
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
    }

    private void setBoardSize(int size) {
        int squareSize = size / 8;
        int actualBoardSize = squareSize * 8;

        setPaneSize(boardPane, actualBoardSize);

        for (Canvas canvas : List.of(boardCanvas, coordsCanvas, piecesCanvas,
                draggingCanvas, coloredSquaresCanvas, drawingCanvas, arrowsCanvas, bitboardCanvas)) {
            canvas.setWidth(actualBoardSize);
            canvas.setHeight(actualBoardSize);
        }

        setPaneSize(boardMaskPane, actualBoardSize);
    }

    public void init() {
        setBoardSize((int) (SCREEN_SIZE * 0.85));
        loadGraphics();
        renderBoard();
        renderCoordinates();
        renderPieces();
        drawBitboard(bitboardCanvas.getGraphicsContext2D(), controller.getDebugBitboard());
        setupMouseEvents();
        startGameLoop();
    }

    private void render() {
        renderColoredSquares();
        renderPieces();
        renderDragging();
        drawBitboard(bitboardCanvas.getGraphicsContext2D(), controller.getDebugBitboard());
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

    private void updateGameState() {
    }

    public void loadGraphics() {
        int squareSize = (int) ((boardCanvas.getWidth()) / 8);
        spritesLoader = new GameSpritesLoader(squareSize);
    }

    private void showHover(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
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
                    int row = getRow(mv.getTo());
                    int col = getCol(mv.getTo());
                    gc.setFill(((row + col) % 2 == 0 ) ? theme.getValidMoveLightSquare() : theme.getValidMoveDarkSquare());
                    gc.fillRect(col * squareSize,row  * squareSize, squareSize, squareSize);
                }
            }
        }
    }

    private void showLastMove(GraphicsContext gc) {
        Move lastMove = controller.getGame().getLastMove();
        if (lastMove != null) {
            int squareSize = (int) (boardCanvas.getWidth() / 8);
            int fromRow = getRow(lastMove.getFrom()), toRow = getRow(lastMove.getTo());
            int fromCol = getCol(lastMove.getFrom()), toCol = getCol(lastMove.getTo());
            gc.setFill(((fromRow + fromCol) % 2 == 0 ) ? theme.getLastMoveLightStartSquare() : theme.getLastMoveDarkStartSquare());
            gc.fillRect(fromCol * squareSize,fromRow  * squareSize, squareSize, squareSize);
            gc.setFill(((toRow + toCol) % 2 == 0 ) ? theme.getLastMoveLightEndSquare() : theme.getLastMoveDarkEndSquare());
            gc.fillRect(toCol * squareSize,toRow  * squareSize, squareSize, squareSize);
        }
    }

    private void showSelectedPiece(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        if (selectedPiece != -1) {
            int row = getRow(selectedPiece);
            int col = getCol(selectedPiece);
            gc.setFill(((row + col) % 2 == 0 ) ? theme.getSelectedLightSquare() : theme.getSelectedDarkSquare());
            gc.fillRect(col * squareSize,row  * squareSize, squareSize, squareSize);
        }
    }

    private void drawPiece(GraphicsContext gc, byte piece, int x, int y) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        gc.drawImage(spritesLoader.getPieceSprite(piece), x, y, squareSize, squareSize);
    }

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
            PieceType piece = position.pieceOnSquare((byte)i);
            if (piece != PieceType.NONE && i != draggedPiece) {
                int row = getRow(i);
                int col = getCol(i);
                drawPiece(gc, piece.id, col * squareSize, row * squareSize);
            }
        }
    }

    private void draggerUpdateBlit(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        Position position = controller.getGame().getPosition();
        drawSelectedPiece(gc, position.pieceOnSquare((byte) selectedPiece).id,
                (int) (mouseXOnBoard - (1.05*squareSize) / 2),
                (int) (mouseYOnBoard - (1.05*squareSize) / 2));
    }

    private void drawCoordinates(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        double fontSize = 24;

        gc.setFont(new Font("Arial", fontSize));

        for (int row = 0; row < 8; row++) {
            gc.setFill(((row % 2) == 0) ? theme.getDarkSquare() : theme.getLightSquare());
            gc.fillText(String.valueOf(isBoardReversed ? row + 1 : 8 - row), 10, row * squareSize + fontSize);
        }

        for (int col = 0; col < 8; col++) {
            gc.setFill(((col % 2) == 0) ? theme.getLightSquare() : theme.getDarkSquare());
            gc.fillText(String.valueOf(isBoardReversed ? (char) ('h' - col) : (char) ('a' + col)),
                    col * squareSize + (squareSize - fontSize), boardCanvas.getHeight() - (fontSize / 2));
        }
    }

    private void drawBoard(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                gc.setFill(((row + col) % 2 == 0 ) ? theme.getLightSquare() : theme.getDarkSquare());
                gc.fillRect(col*squareSize, row*squareSize, squareSize, squareSize);
            }
        }
    }

    private void drawBitboard(GraphicsContext gc, Supplier<Long> bitboardSupplier) {
        long bitboard = bitboardSupplier.get();
        gc.clearRect(0, 0, bitboardCanvas.getWidth(), bitboardCanvas.getHeight());

        double squareSize = bitboardCanvas.getWidth() / 8;
        gc.setFont(new Font("Arial", squareSize / 2));
        double alpha = 0.8;

        for (int i = 0; i < 64; i++) {
            char bit = ((bitboard >>> i) & 1) == 1 ? '1' : '0';

            Text text = new Text(String.valueOf(bit));
            text.setFont(gc.getFont());
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();

            int row = getRow(i);
            int col = getCol(i);

            int x = (int) (col * squareSize + squareSize / 2 - textWidth / 2);
            int y = (int) (row * squareSize + squareSize / 2 + textHeight / 4);

            if (bit == '0') {
                gc.setFill((row + col) % 2 == 0 ? Color.rgb(89, 171, 221, alpha) : Color.rgb(62, 144, 195, alpha));
                gc.fillRect(col*squareSize, row*squareSize, squareSize, squareSize);
            } else {
                gc.setFill((row + col) % 2 == 0 ?  Color.rgb(234,89,102, alpha) : Color.rgb(201,51,69, alpha));
                gc.fillRect(col*squareSize, row*squareSize, squareSize, squareSize);
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
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        int col = Math.min(7, Math.max(0, isBoardReversed ? 7 - (x / squareSize) : x / squareSize));
        int row = Math.min(7, Math.max(0, isBoardReversed ? y / squareSize : 7 - (y / squareSize)));
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
        int squareSize = (int) (drawingCanvas.getWidth() / 8);

        int square = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        isSquareColored[square] = !isSquareColored[square];
        int row = getRow(square);
        int col = getCol(square);

        if (isSquareColored[square]) {
            gc.setFill((row + col) % 2 == 0 ? theme.getDrawingLightSquare() : theme.getDrawingDarkSquare());
            gc.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
        } else {
            double inset = 0.05;
            gc.clearRect(col * squareSize + inset, row * squareSize + inset, squareSize - 2 * inset, squareSize - 2 * inset);
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
        int squareSize = (int)(arrowsCanvas.getWidth() / 8);

        Point2D start = getSquareCenter(arrow.fromSquare, squareSize);
        Point2D end = getSquareCenter(arrow.toSquare, squareSize);

        gc.setStroke(arrow.color);
        gc.setLineWidth(0.14 * squareSize);
        gc.setLineCap(StrokeLineCap.ROUND);

        arrow.draw(gc, start, end, squareSize);
    }

    private void resetSelection() {
        unselectPiece();
        undragPiece();
        previousSelectedPiece = -1;
    }

    private void handleMoveCreation(Position pos, byte selectedPiece, byte squarePos, byte piece, byte color,
                                    Consumer<Move> onMoveReady) {
        int promotionRow = color == PieceIndex.WHITE_PIECES.id ? 7 : 0;
        boolean isPawnMoving = piece == PieceIndex.PAWNS.id;
        boolean hasToPlay = (promotionRow == 7 && pos.isWhiteSideToPlay) || (promotionRow == 0 && !pos.isWhiteSideToPlay);

        Move mv = new Move(selectedPiece, squarePos, piece, color);
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        int row = getRow(mv.getTo());
        int col = getCol(mv.getTo());

        if (mv.getTo() / 8 == promotionRow && isPawnMoving && hasToPlay) {
            mv.setPromoted((byte) 6);
            if (!controller.getGame().isLegal(mv)) return;
            PromotionPopup.showPromotionVBox(boardMaskPane, spritesLoader, color, squareSize, isBoardReversed,
                    col * squareSize, row * squareSize, promotedPiece -> {
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
                    handleInvalidMoveAnimation(mv);
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

    public void setChessController(ChessController controller) {
        this.controller = controller;
        this.theme = controller.getTheme();
        controller.getGame().addMoveListener(this);
    }
}
