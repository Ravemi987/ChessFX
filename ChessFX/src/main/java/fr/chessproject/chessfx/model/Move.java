package fr.chessproject.chessfx.model;

public class Move {

    private final int moveData;

    /* Constructeur complet */
    public Move(byte from, byte to, byte piece, byte color, byte cPiece, byte cColor, byte promoted, byte enPassant) {
        this.moveData = (from & 0x3F) |
                ((to & 0x3F) << 6) |
                ((piece & 0x0F) << 12) |
                ((color & 0x01) << 16) |
                ((cPiece & 0x0F) << 17) |
                ((cColor & 0x01) << 21) |
                ((promoted & 0x0F) << 22) |
                ((enPassant & 0x03) << 26);
    }

    /* Constructeur partiel déplacement simple */
    public Move(byte from, byte to, byte piece, byte color) {
        this.moveData = (from & 0x3F) |
                ((to & 0x3F) << 6) |
                ((piece & 0x0F) << 12) |
                ((color & 0x01) << 16);
    }

    /* Constructeur partiel capture */
    public Move(byte from, byte to, byte piece, byte color, byte cPiece, byte cColor) {
        this.moveData = (from & 0x3F) |
                ((to & 0x3F) << 6) |
                ((piece & 0x0F) << 12) |
                ((color & 0x01) << 16) |
                ((cPiece & 0x0F) << 17) |
                ((cColor & 0x01) << 21);
    }

    /* Constructeur partiel promotion */
    public Move(byte from, byte to, byte piece, byte color, byte cPiece, byte cColor, byte promoted) {
        this.moveData = (from & 0x3F) |
                ((to & 0x3F) << 6) |
                ((piece & 0x0F) << 12) |
                ((color & 0x01) << 16) |
                ((cPiece & 0x0F) << 17) |
                ((cColor & 0x01) << 21) |
                ((promoted & 0x0F) << 22);
    }

    public byte getFrom() {
        return (byte) (moveData & 0x3F);
    }

    public byte getTo() {
        return (byte) ((moveData >> 6) & 0x3F);
    }

    public byte getPiece() {
        return (byte) ((moveData >> 12) & 0x0F);
    }

    public byte getColor() {
        return (byte) ((moveData >> 16) & 0x01);
    }

    public byte getCapturedPiece() {
        return (byte) ((moveData >> 17) & 0x0F);
    }

    public byte getCapturedColor() {
        return (byte) ((moveData >> 21) & 0x01);
    }

    public byte getPromotedPiece() {
        return (byte) ((moveData >> 22) & 0x0F);
    }

    public byte getEnPassant() {
        return (byte) ((moveData >> 26) & 0x03);
    }

    public boolean isCapture() {
        return getCapturedPiece() != 0;
    }

    @Override
    public String toString() {
        return "from=" + getFrom() + " to=" + getTo() + " mPiece=" + getPiece() + " color=" + getColor()
                + " cPiece=" + getCapturedPiece() + " cColor=" + getCapturedColor();
    }

    public boolean equals(Move mv) {
        return getFrom() == mv.getFrom() && getTo() == mv.getTo();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
