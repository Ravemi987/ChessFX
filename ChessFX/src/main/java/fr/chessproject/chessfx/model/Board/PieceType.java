package fr.chessproject.chessfx.model.Board;

public enum PieceType {
    NONE(0),
    WHITE_KING(1),
    WHITE_QUEEN(2),
    WHITE_BISHOP(3),
    WHITE_KNIGHT(4),
    WHITE_ROOK(5),
    WHITE_PAWN(6),
    BLACK_KING(7),
    BLACK_QUEEN(8),
    BLACK_BISHOP(9),
    BLACK_KNIGHT(10),
    BLACK_ROOK(11),
    BLACK_PAWN(12);

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

    public static byte from(byte pieceIndex, byte color) {
        return (byte) (color * 6 + pieceIndex - 1);
    }
}
