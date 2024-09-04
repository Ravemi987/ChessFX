package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.*;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GamePanelController implements Runnable {

    @FXML
    public Pane boardPane;

    @FXML
    public Canvas boardCanvas;

//    @FXML
//    public Pane boardMaskPane;

    private ChessController controller;

    private static final double TICKS_PER_SECOND = 60.0;
    private static final double NS_PER_TICK = 1_000_000_000 / TICKS_PER_SECOND;
    private boolean running = false;
    private GameSpritesLoader spritesLoader;
    private WritableImage staticBoardImage;

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
        render();
        setupMouseEvents();
        //startGameThread();
    }

    public void setupMouseEvents() {
        boardCanvas.setOnMousePressed(this::handleMousePressed);
        boardCanvas.setOnMouseDragged(this::handleMouseDragged);
        boardCanvas.setOnMouseReleased(this::handleMouseReleased);
        boardCanvas.setOnMouseMoved(this::handleMouseMoved);
    }

    public void startGameThread() {
        if (running) return;

        running = true;
        Thread mainThread = new Thread(this);
        mainThread.start();
    }

    // GameLoop

    @Override
    public void run() {
        long lastUpdate = System.nanoTime();
        double delta = 0;
        long timer = System.currentTimeMillis();

        while (running) {
            long currentUpdate = System.nanoTime();
            delta += (currentUpdate - lastUpdate) / NS_PER_TICK;
            lastUpdate = currentUpdate;

            while (delta >= 1) {
                updateGameState();
                delta--;
            }

            render(); //here if needed;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
            }
        }
    }

    private void render() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());
        gc.drawImage(staticBoardImage, 0, 0);

        showSelectedPiece(gc);
        showLastMove(gc);
        showValidMoves(gc);
        drawPieces(gc);
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
        initializeStaticBoardImage();
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

            double width = 3.7;
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
                    gc.setFill(((row + col) % 2 == 0 ) ? Color.rgb(234, 106, 106) : Color.rgb(248, 100, 100));
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
            gc.setFill(((fromRow + fromCol) % 2 == 0 ) ? Color.rgb(218, 196, 49) : Color.rgb(235, 191, 73));
            gc.fillRect(fromCol * squareSize,fromRow  * squareSize, squareSize, squareSize);
            gc.setFill(((toRow + toCol) % 2 == 0 ) ? Color.rgb(218, 196, 49) : Color.rgb(235, 191, 73));
            gc.fillRect(toCol * squareSize,toRow  * squareSize, squareSize, squareSize);
        }
    }

    private void showSelectedPiece(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        if (selectedPiece != -1) {
            int row = 7 - selectedPiece / 8;
            int col = selectedPiece % 8;
            gc.setFill(((row + col) % 2 == 0 ) ? Color.rgb(218, 196, 49) : Color.rgb(235, 191, 73));
            gc.fillRect(col * squareSize,row  * squareSize, squareSize, squareSize);
        }
    }

    private void drawPiece(GraphicsContext gc, byte piece, int x, int y) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        gc.drawImage(spritesLoader.getPieceSprite(piece), x, y, squareSize, squareSize);
    }

    private void drawSelectedPiece(GraphicsContext gc, byte piece, int x, int y) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        ImageView imageView = new ImageView(spritesLoader.getPieceSprite(piece));
        int scaledWidth = (int) (squareSize * 1.05);
        int scaledHeight = (int) (squareSize * 1.05);
        imageView.setFitWidth(scaledWidth);
        imageView.setFitHeight(scaledHeight);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);
        gc.drawImage(imageView.getImage(), x, y, scaledWidth, scaledHeight);
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

    private void drawLastMovedPiece(GraphicsContext gc) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        Move lastMove = controller.getGame().getLastMove();
        Position pos = controller.getGame().getPosition();
        if (lastMove != null) {
            int fromRow = 7 - lastMove.getFrom() / 8, toRow = 7 - lastMove.getTo() / 8;
            int fromCol = lastMove.getFrom() % 8, toCol = lastMove.getTo() % 8;
            gc.clearRect(fromCol * squareSize, fromRow * squareSize, squareSize, squareSize);
            drawPiece(gc, pos.pieceOnSquare(lastMove.getTo()), toCol * squareSize, toRow * squareSize);
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

        gc.setFont(new Font("Arial", 18));

        for (int row = 0; row < 8; row++) {
            gc.setFill(((row % 2) == 0) ? Color.rgb(181, 136, 99) : Color.rgb(240, 217, 181));
            gc.fillText(String.valueOf(8 - row), 10, row * squareSize + 20);
        }

        for (int col = 0; col < 8; col++) {
            gc.setFill(((col % 2) == 0) ? Color.rgb(240, 217, 181) : Color.rgb(181, 136, 99));
            gc.fillText(String.valueOf((char) ('a' + col)), col * squareSize + (squareSize - 20), boardCanvas.getHeight() - 10);
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
            int x = (int) (col * squareSize + (double) squareSize / 2 - textWidth / 2);
            int y = (int) (row * squareSize + (double) squareSize / 2 + textHeight / 4);
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
        drawCoordinates(gc);

        tempCanvas.snapshot(null, staticBoardImage);
    }

    public void updateMousePos(Point2D absoluteMousePos) {

        int x = (int) absoluteMousePos.getX();
        int y = (int) absoluteMousePos.getY();

        if (inBoardRect(x, absoluteMousePos)) {
            mouseXOnBoard = x;
        }
        if (inBoardRect(y, absoluteMousePos)) {
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

    public boolean inBoardRect(int coord, Point2D absoluteMousePos) {
        return (coord == (int) absoluteMousePos.getX()) ? 0 <= coord && coord <= (int) boardCanvas.getWidth()
                : 0 <= coord && coord <= (int) boardCanvas.getHeight();
    }

    public byte getSquareFromPos(int x, int y) {
        int squareSize = (int) (boardCanvas.getWidth() / 8);
        int col = Math.min(7, Math.max(0, x / squareSize));
        int row = Math.min(7, Math.max(0, 7 - (y / squareSize)));
        int squareIndex = col % 8 + row * 8;
        return (byte) squareIndex;
    }

    @FXML
    private void handleMouseReleased(MouseEvent mouseEvent) {
        int releasedSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        Position pos = controller.getGame().getPosition();
        if (!Square.isEmpty((byte) releasedSquare, controller.getGame().getPosition().getOccupied())) {
            releasePiece = releasedSquare;
        } else {
            releasePiece = -1;
        }

        if (previousSelectedPiece != -1 && releasePiece == previousSelectedPiece && clickedPiece == previousSelectedPiece) {
            unselectPiece();
        } else if (selectedPiece != -1) {
            Move mv = new Move((byte) selectedPiece, (byte) releasedSquare, pos.pieceBitboardOnSquare((byte) selectedPiece), pos.pieceColorOnSquare((byte) selectedPiece));
            Move validMove = controller.getGame().checkMove(mv);
            if (validMove != null) controller.getGame().playMove(validMove);
        }

        undragPiece();
        previousSelectedPiece = selectedPiece;

        render();
    }

    @FXML
    private void handleMousePressed(MouseEvent mouseEvent) {
        int clickedSquare = getSquareFromPos(mouseXOnBoard, mouseYOnBoard);
        Position pos = controller.getGame().getPosition();
        if (!Square.isEmpty((byte) clickedSquare, pos.getOccupied())) {
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

        render();
    }

    @FXML
    private void handleMouseDragged(MouseEvent mouseEvent) {
        updateMousePos(new Point2D(mouseEvent.getX(), mouseEvent.getY()));

        render();
    }

    @FXML
    public void handleMouseMoved(MouseEvent mouseEvent) {
        updateMousePos(new Point2D(mouseEvent.getX(), mouseEvent.getY()));
    }

    public void setMainController(ChessController controller) {
        this.controller = controller;
    }
}
