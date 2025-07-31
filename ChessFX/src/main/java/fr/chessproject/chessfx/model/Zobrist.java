package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.helpers.RandomUtilities;

public class Zobrist {

    public static final long[][] pieceSquareKeys = new long[12][64];
    public static final long[] castlingKeys = new long[16];
    public static final long[] enPassantKeys = new long[8];
    public static long sideToMoveKey;

    public static void generateKeys() {
        RandomUtilities.resetSeed();

        for (int piece = 0; piece < 12; piece++) {
            for (int square = 0; square < 64; square++) {
                pieceSquareKeys[piece][square] = RandomUtilities.getRandom64Bits();
            }
        }

        for (int i = 0; i < 16; i++) {
            castlingKeys[i] = RandomUtilities.getRandom64Bits();
        }

        for (int i = 0; i < 8; i++) {
            enPassantKeys[i] = RandomUtilities.getRandom64Bits();
        }

        sideToMoveKey = RandomUtilities.getRandom64Bits();
    }

    public static long getPieceSquareKey(PieceType piece, byte square) {
        return pieceSquareKeys[piece.id][square];
    }

    public static long getCastlingKey(int castlingRights) {
        return castlingKeys[castlingRights];
    }

    public static long getEnPassantKey(byte square) {
        int file = square % 8;
        return enPassantKeys[file];
    }

    public static long getSideToMoveKey() {
        return sideToMoveKey;
    }

    public static long initialize(Position p) {
        long finalKey = 0L;

        for (byte square = 0; square < 64; square++) {
            PieceType piece = p.pieceOnSquare(square);
            if (piece != PieceType.NONE) {
                finalKey ^= getPieceSquareKey(piece, square);
            }
        }

        if (p.enPassantSquare != -1) {
            finalKey ^= getEnPassantKey(p.enPassantSquare);
        }

        finalKey ^= getCastlingKey(p.castlingRights);

        if (!p.isWhiteSideToPlay) {
            finalKey ^= getSideToMoveKey();
        }

        return finalKey;
    }
}
