package fr.chessproject.chessfx.model.Board;

public enum PieceIndex {
    NONE(-1),
    WHITE_PIECES(0),
    BLACK_PIECES(1),
    KINGS(2),
    QUEENS(3),
    BISHOPS(4),
    KNIGHTS(5),
    ROOKS(6),
    PAWNS(7);

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
