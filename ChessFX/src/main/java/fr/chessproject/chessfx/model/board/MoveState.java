package fr.chessproject.chessfx.model.board;

public class MoveState {
    int castlingRights;
    long hash;
    boolean isWhiteSideToPlay;
    byte enPassantSquare;

    public MoveState(Position position) {
        this.castlingRights = position.castlingRights;
        this.isWhiteSideToPlay = position.isWhiteSideToPlay;
        this.enPassantSquare = position.enPassantSquare;
        this.hash = position.hash;
    }

    public MoveState(MoveState other) {
        this.castlingRights = other.castlingRights;
        this.isWhiteSideToPlay = other.isWhiteSideToPlay;
        this.enPassantSquare = other.enPassantSquare;
        this.hash = other.hash;
    }
}
