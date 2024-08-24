package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.helpers.BinaryHelper;

public class Position {

    /* Bitboards indexes */

    private final byte whitePieces = 0;
    private final byte blackPieces = 1;
    private final byte pawns = 2;
    private final byte knights = 3;
    private final byte bishops = 4;
    private final byte rooks = 5;
    private final byte queens = 6;
    private final byte blackKing = 7;
    private final byte whiteKing = 8;

    private final long[] piecesBB;
    private long occupied;
    private long empty;

    private boolean isWhiteSideToPlay;

    public Position() {
        piecesBB = new long[9];
        occupied = empty = 0x0L;
        isWhiteSideToPlay = true;
    }

    public long getPawns() {return piecesBB[pawns];}
    public long getKnights() {return piecesBB[knights];}
    public long getBishops() {return piecesBB[bishops];}
    public long getRooks() {return piecesBB[rooks];}
    public long getQueens() {return piecesBB[queens];}
    public long getWhitePieces() {return piecesBB[whitePieces];}
    public long getBlackPieces() {return piecesBB[blackPieces];}
    public long getBlackKing() {return piecesBB[blackKing];}
    public long getWhiteKing() {return piecesBB[whiteKing];}
    public long getEmpty() {return empty;}
    public long getOccupied() {return occupied;}

    public void loadFEN(String fen) {
        String[] fenTab = fen.split(" ");
        boardFromFEN(fenTab[0]);
    }

    public void boardFromFEN(String fenBoard) {
        int row = 7, col = 0;

        for (char c : fenBoard.toCharArray()) {
            if (c == '/') {
                row--;
                col = 0;
            } else if (Character.isDigit(c)) {
                col += Character.getNumericValue(c);
            } else {
                byte sq = (byte) (row * 8 + col);
                long pos = Square.bitboardForSquare(sq);
                byte piece = Piece.fromChar(c);

                if (Piece.isWhite(piece))
                    piecesBB[whitePieces] |= pos;
                else
                    piecesBB[blackPieces] |= pos;

                addPieceToBitboard(piece, pos);

                col++;
            }
        }
        occupied = piecesBB[whitePieces] | piecesBB[blackPieces];
        empty = ~occupied;
    }

    public void addPieceToBitboard(byte piece, long pos) {
        if (Piece.isPawn(piece)) piecesBB[pawns] |= pos;
        if (Piece.isKnight(piece)) piecesBB[knights] |= pos;
        if (Piece.isBishop(piece)) piecesBB[bishops] |= pos;
        if (Piece.isRook(piece)) piecesBB[rooks] |= pos;
        if (Piece.isQueen(piece)) piecesBB[queens] |= pos;
        if (piece == Piece.WHITE_KING) piecesBB[whiteKing] |= pos;
        if (piece == Piece.BLACK_KING) piecesBB[blackKing] |= pos;
    }

    /* Generate Moves methods */

    public void generateMovesForPiece(MoveList mvList, long moveBB, byte sqFrom, int pieceBB, int colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            Move mv = new Move(sqFrom, sqTo, (byte) pieceBB, (byte) colorBB);
            mvList.addMove(mv);
        }
    }

    public void generateCapturesForPiece(MoveList mvList, long moveBB, byte sqFrom, int pieceBB, int colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            Move mv = new Move(sqFrom, sqTo, (byte) pieceBB, (byte) colorBB, pieceBitboardOnSquare(sqTo), (byte) ((colorBB+1)%2));
            mvList.addMove(mv);
        }
    }

    public void generatePawnMoves(MoveList mvList, long moveBB, int offset, int pieceBB, int colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            byte sqFrom = (byte) (sqTo + offset);
            Move mv = new Move(sqFrom, sqTo, (byte) pieceBB, (byte) colorBB);
            mvList.addMove(mv);
        }
    }

    public void generatePawnCaptures(MoveList mvList, long moveBB, int offset, int pieceBB, int colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            byte sqFrom = (byte) (sqTo + offset);
            Move mv = new Move(sqFrom, sqTo, (byte) pieceBB, (byte) colorBB, pieceBitboardOnSquare(sqTo), (byte) ((colorBB+1)%2));
            mvList.addMove(mv);
        }
    }


    /* ==== Pawns moves ==== */

    public void whitePawnMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[pawns] & piecesBB[whitePieces]; // param
        long singlePush = (piecesBitboard << 8) & empty; // north
        long doublePush = ((singlePush & Square.RANK_3) << 8) & empty;
        long captureLeft = (piecesBitboard & Square.NOT_A_FILE) << 7 & piecesBB[blackPieces]; // north-west
        long captureRight = (piecesBitboard & Square.NOT_H_FILE) << 9 & piecesBB[blackPieces]; // north-east

        generatePawnMoves(mvList, singlePush, -8, pawns, whitePieces);
        generatePawnMoves(mvList, doublePush, -16, pawns, whitePieces);
        generatePawnCaptures(mvList, captureLeft, -7, pawns, whitePieces);
        generatePawnCaptures(mvList, captureRight, -9, pawns, whitePieces);
    }

    public void blackPawnMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[pawns] & piecesBB[blackPieces]; // param
        long singlePush = (piecesBitboard >> 8) & empty; // south
        long doublePush = ((singlePush & Square.RANK_6) >> 8) & empty;
        long captureLeft = (piecesBitboard & Square.NOT_H_FILE) >> 7 & piecesBB[whitePieces];  // south-west
        long captureRight = (piecesBitboard & Square.NOT_A_FILE) >> 9 & piecesBB[whitePieces]; // south-east

        generatePawnMoves(mvList, singlePush, 8, pawns, blackPieces);
        generatePawnMoves(mvList, doublePush, 16, pawns, blackPieces);
        generatePawnCaptures(mvList, captureLeft, 7, pawns, blackPieces);
        generatePawnCaptures(mvList, captureRight, 9, pawns, blackPieces);

    }

    /* ==== Knights moves ==== */

    public void whiteKnightMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[knights] & piecesBB[whitePieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.knightAttacks(sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[blackPieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, knights, whitePieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, knights, whitePieces);
        }
    }

    public void blackKnightMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[knights] & piecesBB[blackPieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.knightAttacks(sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[whitePieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, knights, blackPieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, knights, blackPieces);
        }
    }

    /* ====Kings moves ==== */

    public void whiteKingMoves(MoveList mvList) {
        byte whiteKingSquare = BinaryHelper.bitScanForward(piecesBB[whiteKing]);
        byte blackKingSquare = BinaryHelper.bitScanForward(piecesBB[blackKing]);

        long possibleAttackSquares = Piece.kingAttacks(whiteKingSquare) & ~Piece.kingAttacks(blackKingSquare);
        long moveBitboard = possibleAttackSquares & empty;
        long takeBitboard = possibleAttackSquares & piecesBB[blackPieces];

        generateMovesForPiece(mvList, moveBitboard, whiteKingSquare, whiteKing, whitePieces);
        generateCapturesForPiece(mvList, takeBitboard, whiteKingSquare, whiteKing, whitePieces);
    }

    public void blackKingMoves(MoveList mvList) {
        byte whiteKingSquare = BinaryHelper.bitScanForward(piecesBB[whiteKing]);
        byte blackKingSquare = BinaryHelper.bitScanForward(piecesBB[blackKing]);

        long possibleAttackSquares = Piece.kingAttacks(blackKingSquare) & ~Piece.kingAttacks(whiteKingSquare);
        long moveBitboard = possibleAttackSquares & empty;
        long takeBitboard = possibleAttackSquares & piecesBB[whitePieces];

        generateMovesForPiece(mvList, moveBitboard, blackKingSquare, blackKing, blackPieces);
        generateCapturesForPiece(mvList, takeBitboard, blackKingSquare, blackKing, blackPieces);
    }

    /* ==== Bishops moves ==== */

    public void whiteBishopMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[bishops] & piecesBB[whitePieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.bishopAttacks(occupied, sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[blackPieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, bishops, whitePieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, bishops, whitePieces);
        }
    }

    public void blackBishopMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[bishops] & piecesBB[blackPieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.bishopAttacks(occupied, sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[whitePieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, bishops, blackPieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, bishops, blackPieces);
        }
    }

    /* ==== Rooks moves ==== */

    public void whiteRookMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[rooks] & piecesBB[whitePieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.rookAttacks(occupied, sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[blackPieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, rooks, whitePieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, rooks, whitePieces);
        }
    }

    public void blackRookMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[rooks] & piecesBB[blackPieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.rookAttacks(occupied, sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[whitePieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, bishops, blackPieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, bishops, blackPieces);
        }
    }

    /* ==== Queens moves ==== */

    public void whiteQueenMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[queens] & piecesBB[whitePieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.queenAttacks(occupied, sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[blackPieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, queens, whitePieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, queens, whitePieces);
        }
    }

    public void blackQueenMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[queens] & piecesBB[blackPieces]; // param

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = Piece.queenAttacks(occupied, sqFrom); // param
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[whitePieces]; // param

            generateMovesForPiece(mvList, moveBitboard, sqFrom, queens, blackPieces);
            generateCapturesForPiece(mvList, takeBitboard, sqFrom, queens, blackPieces);
        }
    }

    /* ==== Pseudo-legal moves ==== */

    public MoveList whitesPseudoLegalMoves() {
        MoveList whitesLegalMoves = new MoveList();

        whitePawnMoves(whitesLegalMoves);
        whiteKingMoves(whitesLegalMoves);
        whiteKnightMoves(whitesLegalMoves);
        whiteBishopMoves(whitesLegalMoves);
        whiteRookMoves(whitesLegalMoves);
        whiteQueenMoves(whitesLegalMoves);

        return whitesLegalMoves;
    }

    public MoveList blacksPseudoLegalMoves() {
        MoveList blacksLegalMoves = new MoveList();

        blackPawnMoves(blacksLegalMoves);
        blackKingMoves(blacksLegalMoves);
        blackKnightMoves(blacksLegalMoves);
        blackBishopMoves(blacksLegalMoves);
        blackRookMoves(blacksLegalMoves);
        blackQueenMoves(blacksLegalMoves);

        return blacksLegalMoves;
    }

    public MoveList generatePseudoLegalMoves() {
        return isWhiteSideToPlay ? whitesPseudoLegalMoves() : blacksPseudoLegalMoves();
    }

    public byte pieceBitboardOnSquare(byte sq) {
        long bbSquare = Square.bitboardForSquare(sq);
        if ((piecesBB[pawns] & bbSquare) != 0) return pawns;
        if ((piecesBB[knights] & bbSquare) != 0) return knights;
        if ((piecesBB[bishops] & bbSquare) != 0) return bishops;
        if ((piecesBB[queens] & bbSquare) != 0) return queens;
        if ((piecesBB[rooks] & bbSquare) != 0) return rooks;
        if ((piecesBB[blackKing] & bbSquare) != 0) return blackKing;
        if ((piecesBB[whiteKing] & bbSquare) != 0) return whiteKing;
        return -1;
    }

    public byte pieceColorOnSquare(byte sq) {
        long bbSquare = Square.bitboardForSquare(sq);
        if ((piecesBB[whitePieces] & bbSquare) != 0) return whitePieces;
        if ((piecesBB[blackPieces] & bbSquare) != 0) return blackPieces;
        return -1;
    }

    public byte pieceOnSquare(byte sq) {
        long bbSquare = Square.bitboardForSquare(sq);

        if ((bbSquare & piecesBB[blackKing]) != 0) return Piece.BLACK_KING;
        if ((bbSquare & piecesBB[whiteKing]) != 0) return Piece.WHITE_KING;

        if ((piecesBB[whitePieces] & bbSquare) != 0) {
            if ((piecesBB[pawns] & bbSquare) != 0) return Piece.WHITE_PAWN;
            if ((piecesBB[knights] & bbSquare) != 0) return Piece.WHITE_KNIGHT;
            if ((piecesBB[bishops] & bbSquare) != 0) return Piece.WHITE_BISHOP;
            if ((piecesBB[rooks] & bbSquare) != 0) return Piece.WHITE_ROOK;
            if ((piecesBB[queens] & bbSquare) != 0) return Piece.WHITE_QUEEN;
            return Piece.NONE;
        }

        if ((piecesBB[blackPieces] & bbSquare) != 0) {
            if ((piecesBB[pawns] & bbSquare) != 0) return Piece.BLACK_PAWN;
            if ((piecesBB[knights] & bbSquare) != 0) return Piece.BLACK_KNIGHT;
            if ((piecesBB[bishops] & bbSquare) != 0) return Piece.BLACK_BISHOP;
            if ((piecesBB[rooks] & bbSquare) != 0) return Piece.BLACK_ROOK;
            if ((piecesBB[queens] & bbSquare) != 0) return Piece.BLACK_QUEEN;
        }

        return Piece.NONE;
    }

    /* makeMove and unmakeMove */

    public void makeMove(Move move) {
        long fromBB = 0x1L << move.getFrom();
        long toBB = 0x1L << move.getTo();
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            piecesBB[move.getCapturedPiece()] ^= toBB;
            piecesBB[move.getCapturedColor()] ^= toBB;
            occupied ^= fromBB;
            empty ^= fromBB;
        } else {
            occupied ^= fromToBB;
            empty ^= fromToBB;
        }

        piecesBB[move.getPiece()] ^= fromToBB;
        piecesBB[move.getColor()] ^= fromToBB;

        isWhiteSideToPlay = !isWhiteSideToPlay;
    }

    public void unmakeMove(Move move) {
        long fromBB = 0x1L << move.getFrom();
        long toBB = 0x1L << move.getTo();
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            piecesBB[move.getCapturedPiece()] ^= toBB;
            piecesBB[move.getCapturedColor()] ^= toBB;
            occupied ^= fromBB;
            empty ^= fromBB;
        } else {
            occupied ^= fromToBB;
            empty ^= fromToBB;
        }

        piecesBB[move.getPiece()] ^= fromToBB;
        piecesBB[move.getColor()] ^= fromToBB;

        isWhiteSideToPlay = !isWhiteSideToPlay;
    }

    public static Position copy(Position pos) {
        Position newPos = new Position();
        newPos.occupied = pos.occupied;
        newPos.empty = pos.empty;
        newPos.isWhiteSideToPlay = pos.isWhiteSideToPlay;

        System.arraycopy(pos.piecesBB, 0, newPos.piecesBB, 0, pos.piecesBB.length);

        return newPos;
    }
}
