package fr.chessproject.chessfx.model;

public class MoveState {
    boolean isAllowedWhiteShortCastle;
    boolean isAllowedWhiteLongCastle;
    boolean isAllowedBlackShortCastle;
    boolean isAllowedBlackLongCastle;
    boolean isWhiteSideToPlay;

    public MoveState(Position position) {
        this.isAllowedWhiteShortCastle = position.isAllowedWhiteShortCastle;
        this.isAllowedWhiteLongCastle = position.isAllowedWhiteLongCastle;
        this.isAllowedBlackShortCastle = position.isAllowedBlackShortCastle;
        this.isAllowedBlackLongCastle = position.isAllowedBlackLongCastle;
        this.isWhiteSideToPlay = position.isWhiteSideToPlay;
    }
}
