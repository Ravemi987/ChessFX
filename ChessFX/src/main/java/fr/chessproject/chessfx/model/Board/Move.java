package fr.chessproject.chessfx.model.Board;

public class Move {

    private int moveData;
    private static final char[] promotionsChars = new char[]{'n', 'b', 'r', 'q'};

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

    /* Constructeurs pour comparaisons */

    public Move(byte from, byte to) {
        this.moveData = (from & 0x3F) |
                ((to & 0x3F) << 6);
    }

    public Move(byte from, byte to, byte promoted) {
        this.moveData = (from & 0x3F) |
                ((to & 0x3F) << 6) |
                ((promoted & 0x0F) << 22);
    }

    /* getters */

    public byte getFrom() {
        return (byte) (moveData & 0x3F);
    }

    public byte getTo() {
        return (byte) ((moveData >>> 6) & 0x3F);
    }

    public byte getPiece() {
        return (byte) ((moveData >>> 12) & 0x0F);
    }

    public byte getColor() {
        return (byte) ((moveData >>> 16) & 0x01);
    }

    public byte getCapturedPiece() {
        return (byte) ((moveData >>> 17) & 0x0F);
    }

    public byte getCapturedColor() {
        return (byte) ((moveData >>> 21) & 0x01);
    }

    public byte getPromotedPiece() {
        return (byte) ((moveData >>> 22) & 0x0F);
    }

    public byte getEnPassant() {
        return (byte) ((moveData >>> 26) & 0x3F);
    }

    /* setters */

    public void setPromoted(byte promoted) {
        moveData = (moveData & ~(0x0F << 22)) | ((promoted & 0x0F) << 22);
    }

    /* others */

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

    public static Move convertFromString(String mvStr) {
        char fromFileChar = mvStr.charAt(0);
        char fromRankChar = mvStr.charAt(1);
        char toFileChar = mvStr.charAt(2);
        char toRankChar = mvStr.charAt(3);

        int fromCol = fromFileChar - 'a';
        int fromRow = fromRankChar - '1';
        int toCol = toFileChar - 'a';
        int toRow = toRankChar - '1';

        int fromIndex = fromRow * 8 + fromCol;
        int toIndex = toRow * 8 + toCol;

        if (mvStr.length() == 5) {
            char promoChar = mvStr.charAt(4);
            byte promotedPiece = (byte) (3 + new String(promotionsChars).indexOf(promoChar));
            return new Move((byte) fromIndex, (byte) toIndex, promotedPiece);
        } else {
            return new Move((byte) fromIndex, (byte) toIndex);
        }
    }

    @Override
    public String toString() {
        int fromRow = 1 + getFrom() / 8;
        int toRow = 1 + getTo() / 8;
        int fromCol = getFrom() % 8;
        int toCol = getTo() % 8;
        return Character.toString('a' + fromCol) + fromRow
                + Character.toString('a' + toCol) + toRow
                + (isPromotion() ? promotionsChars[getPromotedPiece() - 3] : ' ');

    }

    public boolean equals(Move mv) {
        return getFrom() == mv.getFrom() && getTo() == mv.getTo() && getPromotedPiece() == mv.getPromotedPiece();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
