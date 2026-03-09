package fr.chessproject.chessfx.model.game;

import fr.chessproject.chessfx.model.board.Move;

public interface MoveListener {
    void onMovePlayed(Move move);
    void onGameOver();
    void onGameStarted();
}
