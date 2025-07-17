package fr.chessproject.chessfx.controller;

import fr.chessproject.chessfx.main.Divide;
import fr.chessproject.chessfx.main.Perft;
import fr.chessproject.chessfx.model.CommandListenerObserver;
import fr.chessproject.chessfx.model.Game;
import fr.chessproject.chessfx.model.Move;
import fr.chessproject.chessfx.model.Piece;
import fr.chessproject.chessfx.view.Config;
import fr.chessproject.chessfx.view.MainFrameController;
import fr.chessproject.chessfx.view.Theme;

import java.util.Arrays;
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
        nThreads = Runtime.getRuntime().availableProcessors();
        this.game = new Game();
        this.config = new Config();
    }

    @Override
    public void onCommandReceived(String command, String[] args) {
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

    private void handleDisplayCommand() {
        System.out.println(game.getFen());
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
            frameController.updateBoard();
            return;
        }

        for (String move : movesStr) {
            Move finalMove = game.checkMoveFomString(move);
            if (finalMove == null) {
                return;
            }
            game.playMove(finalMove);

        }
        frameController.updateBoard();
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

    public void setFrameController(MainFrameController frameController) {
        this.frameController = frameController;
    }

    public void initDialog() {
        frameController.resetGUI();
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
        return game.getAttackInfoPinnedPices();
    }

    public long getEpBitboard() {
        return game.getEpBitboard();
    }

    public Supplier<Long> getDebugBitboard() {
        return this::getEpBitboard;
    }
}
