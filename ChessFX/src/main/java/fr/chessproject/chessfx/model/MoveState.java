package fr.chessproject.chessfx.model;

public class MoveState {
    boolean isAllowedWhiteShortCastle;
    boolean isAllowedWhiteLongCastle;
    boolean isAllowedBlackShortCastle;
    boolean isAllowedBlackLongCastle;
    boolean isWhiteSideToPlay;
    byte enPassantSquare;

    public MoveState(Position position) {
        this.isAllowedWhiteShortCastle = position.isAllowedWhiteShortCastle;
        this.isAllowedWhiteLongCastle = position.isAllowedWhiteLongCastle;
        this.isAllowedBlackShortCastle = position.isAllowedBlackShortCastle;
        this.isAllowedBlackLongCastle = position.isAllowedBlackLongCastle;
        this.isWhiteSideToPlay = position.isWhiteSideToPlay;
        this.enPassantSquare = position.enPassantSquare;
    }

    public MoveState(MoveState other) {
        this.isAllowedWhiteShortCastle = other.isAllowedWhiteShortCastle;
        this.isAllowedWhiteLongCastle = other.isAllowedWhiteLongCastle;
        this.isAllowedBlackShortCastle = other.isAllowedBlackShortCastle;
        this.isAllowedBlackLongCastle = other.isAllowedBlackLongCastle;
        this.isWhiteSideToPlay = other.isWhiteSideToPlay;
        this.enPassantSquare = other.enPassantSquare;
    }

    @Override
    public String toString() {
        return "MoveState{" +
                "isAllowedWhiteShortCastle=" + isAllowedWhiteShortCastle +
                ", isAllowedWhiteLongCastle=" + isAllowedWhiteLongCastle +
                ", isAllowedBlackShortCastle=" + isAllowedBlackShortCastle +
                ", isAllowedBlackLongCastle=" + isAllowedBlackLongCastle +
                ", isWhiteSideToPlay=" + isWhiteSideToPlay +
                ", enPassantSquare=" + enPassantSquare +
                '}';
    }
}
