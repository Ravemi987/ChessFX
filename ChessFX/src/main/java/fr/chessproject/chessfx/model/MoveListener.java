package fr.chessproject.chessfx.model;

public interface MoveListener {
    void onMovePlayed(Move move, Position positionBefore, Position positionAfter);
}
