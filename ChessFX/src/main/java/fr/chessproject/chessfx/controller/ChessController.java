package fr.chessproject.chessfx.controller;

import fr.chessproject.chessfx.model.Game;
import fr.chessproject.chessfx.view.MainFrameController;

public class ChessController {

    private MainFrameController frame;
    private final Game game;

    public ChessController(Game g) {
        this.game = g;
    }

    public void initDialog() {
        frame = new MainFrameController();
        frame.setDialog(this);
        frame.initFrame();
        frame.setVisible(true);
        frame.resetGUI();
    }

    public void enableDebugMode() {
        frame.enableDebugMode();
    }

    public Game getGame() {
        return game;
    }

    public void loadFEN(String fen) {
        game.getPosition().loadFEN(fen);
    }
}
