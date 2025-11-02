package fr.chessproject.chessfx.model;

public interface MoveListener {
    void onMovePlayed(Move move);
    void onGameOver();
    void onGameStarted();
}
