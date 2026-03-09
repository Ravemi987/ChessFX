package fr.chessproject.chessfx.controller;

import fr.chessproject.chessfx.model.engine.Divide;
import fr.chessproject.chessfx.model.engine.Perft;
import fr.chessproject.chessfx.model.game.Game;
import fr.chessproject.chessfx.model.board.Move;
import fr.chessproject.chessfx.model.board.Piece;
import fr.chessproject.chessfx.model.board.Zobrist;
import fr.chessproject.chessfx.model.uci.CommandListenerObserver;
import fr.chessproject.chessfx.view.Config;
import fr.chessproject.chessfx.view.MainFrameController;
import fr.chessproject.chessfx.view.Theme;
import javafx.application.Platform;
// import javafx.scene.layout.StackPane;

import java.util.function.Supplier;

public class ChessController implements CommandListenerObserver {

    private MainFrameController frameController;
    private final Game game;
    private final Config config;
    private int nThreads;
    public static long[][] rookMovesLookup;
    public static long[][] bishopMovesLookup;

    public ChessController() {
        rookMovesLookup = Piece.generateMovesLookup(false);
        bishopMovesLookup = Piece.generateMovesLookup(true);
        Zobrist.generateKeys();
        nThreads = Runtime.getRuntime().availableProcessors();
        this.game = new Game();
        this.config = new Config();
    }

    @Override
    public void onCommandReceived(String command, String... args) {
        switch (command) {
            case "display":
                handleDisplayCommand();
                break;
            case "position":
                handlePositionCommand(args);
                break;
            case "go":
                handleGoCommand(args);
            case "setoption":
                handleSetOptionCommand(args);
            default:
                break;
        }
    }

    private void updateFrame() {
        Platform.runLater(() -> frameController.updateBoard());
    }

    private void handleDisplayCommand() {
        game.getPosition().printBoard();
        System.out.print("\n");
        System.out.println("Fen: " + game.getFen());
        System.out.println("Zobrist Key: " + game.getPosition().getHash());
        System.out.println("Recomputed Zobrist Key: " + Zobrist.computeHash(game.getPosition()));
    }

    private void handlePositionCommand(String[] s) {
        String[] movesStr = null;
        if (s[0].equals("startpos")) {
            game.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            if (!s[1].isEmpty()) movesStr = s[2].split(" ");
        } else {
            game.setFen(s[1]);
            if (!s[2].isEmpty()) movesStr = s[3].split(" ");
        }

        if (movesStr == null) {
            updateFrame();
            return;
        }

        for (String move : movesStr) {
            Move finalMove = game.checkMoveFomString(move);
            if (finalMove == null) {
                return;
            }
            game.playMoveCLI(finalMove);

        }
        updateFrame();
    }

    private void handleGoCommand(String[] s) {
        if (s[0].equals("perft")) {
            Perft perft = new Perft(game.getFen());
            perft.runPerft(Integer.parseInt(s[1]), nThreads);
        } else {
            Divide divide = new Divide(game.getFen());
            divide.runDivide(Integer.parseInt(s[1]), nThreads);
        }
    }

    private void handleSetOptionCommand(String[] s) {
        if (s[0].equals("threads")) {
            nThreads = Integer.parseInt(s[1]);
        }
    }

    public void setGameClocks(StackPane clk1, StackPane clk2) {
        game.setClocks(clk1, clk2);
    }

    public void startNewGame() {
        game.reset();
        game.startCompetitiveGame();
    }

    public void setFrameController(MainFrameController frameController) {
        this.frameController = frameController;
    }

    public void enableDebugMode() {
        frameController.enableDebugMode();
    }

    public Game getGame() {
        return game;
    }

    public Theme getTheme() {
        return config.getTheme();
    }

    // Helpers

    public long getAttackInfoCheckMask() {
        return game.getAttackInfoCheckMask();
    }

    public long getAttackInfoAttackMask() {
        return game.getAttackInfoAttackMask();
    }

    public long getAttackInfoPinnedPices() {
        return game.getAttackInfoPinnedPieces();
    }

    public long getEpBitboard() {
        return game.getEpBitboard();
    }

    public Supplier<Long> getDebugBitboard() {
        return this::getEpBitboard;
    }
}
