package fr.chessproject.chessfx.model.Game;

import fr.chessproject.chessfx.model.Board.Move;

public interface MoveListener {
    void onMovePlayed(Move move);
    void onGameOver();
    void onGameStarted();
}
