package fr.chessproject.chessfx.controller;

import fr.chessproject.chessfx.model.Game;
import fr.chessproject.chessfx.view.MainFrameController;

public class ChessController {

    private MainFrameController frameController;
    private final Game game;

    public ChessController(Game g) {
        this.game = g;
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

    public void loadFEN(String fen) {
        game.getPosition().loadFEN(fen);
    }
}
