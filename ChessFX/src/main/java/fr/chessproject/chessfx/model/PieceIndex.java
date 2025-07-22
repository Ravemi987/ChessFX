package fr.chessproject.chessfx.model;

public enum PieceIndex {
    NONE(-1),
    WHITE_PIECES(0),
    BLACK_PIECES(1),
    PAWNS(2),
    KNIGHTS(3),
    BISHOPS(4),
    ROOKS(5),
    QUEENS(6),
    BLACK_KING(7),
    WHITE_KING(8);

    public final byte id;

    PieceIndex(int id) {
        this.id = (byte) id;
    }

    public static PieceIndex fromIndex(byte idx) {
        for (PieceIndex b : values()) {
            if (b.id == idx) return b;
        }
        return null;
    }
}
