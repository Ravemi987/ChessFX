package fr.chessproject.chessfx.model;

public class Move {

    private final int moveData;

    /*
    | 6 bits | 6 bits | 4 bits | 1 bit | 4 bits | 1 bit | 4 bits | 6 bits | = 32 bits
    | from   | to     | piece  | color | cPiece | cColor| promoted| enPassant |
     */

    /* Constructeur complet */
    public Move(byte from, byte to, byte piece, byte color, byte cPiece, byte cColor, byte promoted, byte enPassant) {
        this.moveData = (from & 0x3F) |
                ((to & 0x3F) << 6) |
                ((piece & 0x0F) << 12) |
                ((color & 0x01) << 16) |
                ((cPiece & 0x0F) << 17) |
                ((cColor & 0x01) << 21) |
                ((promoted & 0x0F) << 22) |
                ((enPassant & 0x3F) << 26);
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
        return (byte) ((moveData >> 26) & 0x3F);
    }

    public boolean isCapture() {
        return getCapturedPiece() != 0;
    }

    public boolean isPromotion() {
        return getPromotedPiece() != 0;
    }

    public boolean isEnPassant() {
        return getEnPassant() != 0;
    }

    /* Pre: isPawn(getPiece()) */
    public boolean isDoublePawnPush() {
        return Math.abs(getFrom() - getTo()) == 16;
    }

    @Override
    public String toString() {
        int fromRow = 1 + getFrom() / 8;
        int toRow = 1 + getTo() / 8;
        int fromCol = getFrom() % 8;
        int toCol = getTo() % 8;
//        return Character.toString('a' + fromRow) + fromCol
//                + Character.toString('a' + (toRow + 1)) + toCol;
        return Character.toString('a' + fromCol) + fromRow
                + Character.toString('a' + toCol) + toRow;

    }

    public boolean equals(Move mv) {
        return getFrom() == mv.getFrom() && getTo() == mv.getTo();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
