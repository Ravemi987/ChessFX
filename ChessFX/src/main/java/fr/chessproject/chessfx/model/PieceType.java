package fr.chessproject.chessfx.model;

public enum PieceType {
    NONE(-1),
    WHITE_KING(0),
    WHITE_QUEEN(1),
    WHITE_BISHOP(2),
    WHITE_KNIGHT(3),
    WHITE_ROOK(4),
    WHITE_PAWN(5),
    BLACK_KING(6),
    BLACK_QUEEN(7),
    BLACK_BISHOP(8),
    BLACK_KNIGHT(9),
    BLACK_ROOK(10),
    BLACK_PAWN(11);

    public final byte id;

    PieceType(int id) {
        this.id = (byte) id;
    }

    public static PieceType fromId(byte id) {
        for (PieceType p : values()) {
            if (p.id == id) return p;
        }
        return NONE;
    }

    public static PieceType from(byte pieceIndex, byte color) {
        return fromId((byte) (color * 6 + pieceIndex));
    }
}
