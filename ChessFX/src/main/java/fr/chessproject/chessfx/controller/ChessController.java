package fr.chessproject.chessfx.controller;

import fr.chessproject.chessfx.main.Divide;
import fr.chessproject.chessfx.main.Perft;
import fr.chessproject.chessfx.model.CommandListenerObserver;
import fr.chessproject.chessfx.model.Game;
import fr.chessproject.chessfx.view.MainFrameController;

public class ChessController implements CommandListenerObserver {

    private MainFrameController frameController;
    private final Game game;

    public ChessController(Game game) {
        this.game = game;
    }

    @Override
    public void onCommandReceived(String command, String args) {
        switch (command) {
            case "getpos":
                handleGetPosCommand();
                break;
            case "setpos":
                handleSetPosCommand(args);
                break;
            case "perft":
                handlePerftCommand(args);
                break;
            case "divide":
                handleDivideCommand(args);
                break;
            default:
                break;
        }
    }

    private int[] checkPerftDivideCondition(String s) {
        String[] args = s.split(" ");
        int depth, nThreads;

        if (args.length != 2) {
            System.out.println("Incorrect arguments");
            return null;
        } else {
            try {
                depth = Integer.parseInt(args[0]);
                nThreads = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect arguments");
                return null;
            }
        }
        return new int[]{depth, nThreads};
    }

    private void handleGetPosCommand() {
        System.out.println(game.getFen());
    }

    private void handleSetPosCommand(String s) {
        if (s.equals("startpos")) {
            game.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        } else {
            game.setFen(s);
        }
        frameController.updateBoard();
    }

    private void handlePerftCommand(String s) {
        Perft perft = new Perft(game.getFen());
        int[] args = checkPerftDivideCondition(s);
        if (args != null) perft.runPerft(args[0], args[1]);
    }

    private void handleDivideCommand(String s) {
        Divide divide = new Divide(game.getFen());
        int[] args = checkPerftDivideCondition(s);
        if (args != null) divide.runDivide(args[0], args[1]);
    }

    public void setFrameController(MainFrameController frameController) {
        this.frameController = frameController;
    }

    public void initDialog() {
        frameController.setVisible(true);
        frameController.resetGUI();
    }

    public void enableDebugMode() {
        frameController.enableDebugMode();
    }

    public Game getGame() {
        return game;
    }
}
