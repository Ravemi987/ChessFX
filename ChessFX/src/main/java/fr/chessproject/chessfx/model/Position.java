package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.helpers.BinaryHelper;

import java.util.Stack;

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

    public boolean isAllowedBlackShortCastle;
    public boolean isAllowedBlackLongCastle;
    public boolean isAllowedWhiteShortCastle;
    public boolean isAllowedWhiteLongCastle;

    public boolean isWhiteSideToPlay;
    public byte enPassantSquare;
    public int halfMoveClock;
    public int fullMoveCounter;

    private long[] piecesBB;
    private long occupied;
    private long empty;
    private Stack<MoveState> moveStateHistory;

    public Position() {
        reset();
    }

    public void reset() {
        this.piecesBB = new long[9];
        occupied = empty = 0x0L;
        isWhiteSideToPlay = true;
        enPassantSquare = -1;
        halfMoveClock = 0;
        fullMoveCounter = 1;
        isAllowedBlackShortCastle = isAllowedBlackLongCastle = isAllowedWhiteShortCastle = isAllowedWhiteLongCastle = false;
        moveStateHistory = new Stack<>();
    }

    public long getOccupied() {
        return occupied;
    }

    public byte getKingSquare(byte color) {
        long kingBB = piecesBB[color == 0 ? whiteKing : blackKing];
        return (byte) Long.numberOfTrailingZeros(kingBB);
    }

    public byte getOpponentColor(byte color) {
        return (byte) ((color + 1) % 2);
    }

    /* ################### FEN ################### */

    public void loadFEN(String fen) {
        String[] fenArr = fen.split(" ");
        if (fenArr.length < 4 || fenArr.length > 6) {
            System.out.println("Invalid FEN");
            return;
        }
        int returnCode = 0;

        returnCode += setBoardFromFEN(fenArr[0]);
        returnCode += setSideToMove(fenArr[1]);
        returnCode += setCastlingCapabilities(fenArr[2]);
        returnCode += setEpTargetSquare(fenArr[3]);

        if (fenArr.length > 4) {
            returnCode += setHalfmoveClock(fenArr[4]);
            returnCode += setFullmoveCounter(fenArr[5]);
        }

        if (returnCode != 0) {
            System.out.println("Invalid FEN");
        }
    }

    public int setBoardFromFEN(String fenBoard) {
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
                if (piece == Piece.NONE) {
                    return -1;
                }

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

        return 0;
    }

    public void addPieceToBitboard(byte piece, long pos) {
        if (Piece.isPawn(piece)) piecesBB[pawns] |= pos;
        else if (Piece.isKnight(piece)) piecesBB[knights] |= pos;
        else if (Piece.isBishop(piece)) piecesBB[bishops] |= pos;
        else if (Piece.isRook(piece)) piecesBB[rooks] |= pos;
        else if (Piece.isQueen(piece)) piecesBB[queens] |= pos;
        else if (piece == Piece.WHITE_KING) piecesBB[whiteKing] |= pos;
        else if (piece == Piece.BLACK_KING) piecesBB[blackKing] |= pos;
    }

    public int setSideToMove(String fenSide) {
        if (fenSide.equals("w")) {
            isWhiteSideToPlay = true;
        } else if (fenSide.equals("b")) {
            isWhiteSideToPlay = false;
        } else {
            return -1;
        }
        return 0;
    }

    public int setCastlingCapabilities(String fenCastling) {
        for (char c : fenCastling.toCharArray()) {
            if (c == '-') {
                return 0;
            } else if (c == 'K') {
                isAllowedWhiteShortCastle = true;
            } else if (c == 'Q') {
                isAllowedWhiteLongCastle = true;
            } else if (c == 'k') {
                isAllowedBlackShortCastle = true;
            } else if (c == 'q') {
                isAllowedBlackLongCastle = true;
            } else {
                return -1;
            }
        }
        return 0;
    }

    public int setEpTargetSquare(String fenEp) {
        if (fenEp.equals("-")) {
            enPassantSquare = -1;
            return 0;
        }
        int fileIndex = fenEp.charAt(0) - 'a';
        int rankIndex = Character.getNumericValue(fenEp.charAt(1)) - 1;
        int sq = 8 * rankIndex + fileIndex;

        if (0 <= sq && sq < 64) {
            enPassantSquare = (byte) sq;
            return 0;
        }
        return -1;
    }

    public int setHalfmoveClock(String fenMv) {
        halfMoveClock = Integer.parseInt(fenMv);
        return 0;
    }

    public int setFullmoveCounter(String fenMv) {
        fullMoveCounter = Integer.parseInt(fenMv);
        return 0;
    }

    public String getFEN() {
        return getBoardFEN() + " " +
                (isWhiteSideToPlay ? "w" : "b") + " " +
                getCastlingFEN() + " " +
                getEpFEN() + " " +
                halfMoveClock + " " +
                fullMoveCounter;
    }

    private String getBoardFEN() {
        StringBuilder fen = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < 8; file++) {
                byte sq = (byte) (rank * 8 + file);
                char pieceChar = getPieceAtSquare(sq);

                if (pieceChar == ' ') {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }
                    fen.append(pieceChar);
                }
            }
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            if (rank > 0) {
                fen.append('/');
            }
        }
        return fen.toString();
    }

    private char getPieceAtSquare(byte sq) {
        long pos = Square.bitboardForSquare(sq);
        if ((piecesBB[pawns] & pos) != 0) return (piecesBB[whitePieces] & pos) != 0 ? 'P' : 'p';
        if ((piecesBB[knights] & pos) != 0) return (piecesBB[whitePieces] & pos) != 0 ? 'N' : 'n';
        if ((piecesBB[bishops] & pos) != 0) return (piecesBB[whitePieces] & pos) != 0 ? 'B' : 'b';
        if ((piecesBB[rooks] & pos) != 0) return (piecesBB[whitePieces] & pos) != 0 ? 'R' : 'r';
        if ((piecesBB[queens] & pos) != 0) return (piecesBB[whitePieces] & pos) != 0 ? 'Q' : 'q';
        if ((piecesBB[whiteKing] & pos) != 0) return 'K';
        if ((piecesBB[blackKing] & pos) != 0) return 'k';
        return ' ';
    }

    private String getCastlingFEN() {
        StringBuilder sb = new StringBuilder();
        if (isAllowedWhiteShortCastle) sb.append('K');
        if (isAllowedWhiteLongCastle) sb.append('Q');
        if (isAllowedBlackShortCastle) sb.append('k');
        if (isAllowedBlackLongCastle) sb.append('q');
        return (sb.isEmpty()) ? "-" : sb.toString();
    }

    private String getEpFEN() {
        if (enPassantSquare == -1) return "-";
        int file = enPassantSquare % 8;
        int rank = enPassantSquare / 8;
        return (char) ('a' + file) + Integer.toString(rank + 1);
    }


    /* ################### CHECKCOUNT AND SQUAREDATTACK ################### */

    public int checkCount(byte sq, byte colorIdx) {
        int count = 0;
        long attackers;

        long Knights = piecesBB[colorIdx] & piecesBB[knights];

        if (Knights != 0) {
            attackers = Knights & Piece.knightAttacks(sq);
            if ((count += Long.bitCount(attackers)) >= 2) return count;
        }

        long Pawns = piecesBB[colorIdx] & piecesBB[pawns];
        if (Pawns != 0) {
            attackers = Pawns & (colorIdx == 0 ? Piece.blackPawnAttacks(sq) : Piece.whitePawnAttacks(sq));
            if ((count += Long.bitCount(attackers)) >= 2) return count;
        }

        long Rooks = piecesBB[colorIdx] & piecesBB[rooks];
        long rookAttacks = 0L;
        boolean isRooksComputed = false;
        if (Rooks != 0) {
            rookAttacks = Piece.rookAttacks(occupied, sq);
            attackers = Rooks & rookAttacks;
            if ((count += Long.bitCount(attackers)) >= 2) return count;
            isRooksComputed = true;
        }

        long Bishops = piecesBB[colorIdx] & piecesBB[bishops];
        long bishopAttacks = 0L;
        boolean isBishopComputed = false;
        if (Bishops != 0) {
            bishopAttacks = Piece.bishopAttacks(occupied, sq);
            attackers = Bishops & bishopAttacks;
            if ((count += Long.bitCount(attackers)) >= 2) return count;
            isBishopComputed = true;
        }

        long Queens = piecesBB[colorIdx] & piecesBB[queens];
        if (Queens != 0) {
            if (!isRooksComputed) rookAttacks = Piece.rookAttacks(occupied, sq);
            if (!isBishopComputed) bishopAttacks = Piece.bishopAttacks(occupied, sq);
            attackers = Queens & (rookAttacks | bishopAttacks);
            if ((count += Long.bitCount(attackers)) >= 2) return count;
        }
        return count;
    }

    public boolean isSquareAttacked(byte sq, byte colorIdx) {
        long Knights = piecesBB[colorIdx] & piecesBB[knights];
        if ((Knights != 0) && (Knights & Piece.knightAttacks(sq)) != 0) {
            return true;
        }

        long Pawns = piecesBB[colorIdx] & piecesBB[pawns];
        if ((Pawns != 0) && (Pawns & (colorIdx == 1 ?
                Piece.whitePawnAttacks(sq) : Piece.blackPawnAttacks(sq))) != 0)  {
            return true;
        }

        long Rooks = piecesBB[colorIdx] & piecesBB[rooks];
        long rookAttacks = 0L;
        boolean isRooksComputed = false;
        if (Rooks != 0) {
            rookAttacks = Piece.rookAttacks(occupied, sq);
            if ((Rooks & rookAttacks) != 0) {
                return true;
            }
            isRooksComputed = true;
        }

        long Bishops = piecesBB[colorIdx] & piecesBB[bishops];
        long bishopAttacks = 0L;
        boolean isBishopComputed = false;
        if (Bishops != 0) {
            bishopAttacks = Piece.bishopAttacks(occupied, sq);
            if ((Bishops & bishopAttacks) != 0) {
                return true;
            }
            isBishopComputed = true;
        }

        long Queens = piecesBB[colorIdx] & piecesBB[queens];
        if (Queens != 0) {
            if (!isRooksComputed) rookAttacks = Piece.rookAttacks(occupied, sq);
            if (!isBishopComputed) bishopAttacks = Piece.bishopAttacks(occupied, sq);
            if ((Queens & (rookAttacks | bishopAttacks)) != 0) {
                return true;
            }
        }
        return ((piecesBB[colorIdx == 0 ? whiteKing : blackKing] & Piece.kingAttacks(sq)) != 0);
    }

    public boolean checkKingSafety(Move mv) {
        byte color = mv.getColor();
        boolean isKingSafe = true;

        if (color == 0) makeWhiteMoveBitboardOnly(mv); else makeBlackMoveBitboardOnly(mv);

        if (isSquareAttacked(getKingSquare(color), getOpponentColor(color))) {
            isKingSafe = false;
        }

        if (color == 0) unmakeMoveWhite(mv); else unmakeMoveBlack(mv);

        return isKingSafe;
    }

    /* ################### PIECES MOVES ################### */

    public void generatePieceMoves(MoveList mvList, long moveBB, byte sqFrom, byte pieceBB, byte colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB);
            if (checkKingSafety(mv)) {
                mvList.addMove(mv);
            }
        }
    }

    public void generatePieceCaptures(MoveList mvList, long moveBB, byte sqFrom, byte pieceBB, byte colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB, pieceBitboardOnSquare(sqTo), getOpponentColor(colorBB));
            if (checkKingSafety(mv)) {
                mvList.addMove(mv);
            }
        }
    }

    public void generatePawnMoves(MoveList mvList, long moveBB, int offset, int promotionRow, byte pieceBB, byte colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            byte sqFrom = (byte) (sqTo + offset);

            if (sqTo / 8 == promotionRow) {
                generatePromotionMoves(mvList, sqFrom, sqTo, pieceBB, colorBB, (byte) 0, (byte) 0);
            } else {
                Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB);
                if (checkKingSafety(mv)) {
                    mvList.addMove(mv);
                }
            }
        }
    }

    public void generatePawnCaptures(MoveList mvList, long moveBB, int offset, int promotionRow, byte pieceBB, byte colorBB) {
        while(moveBB != 0) {
            long piece = Long.lowestOneBit(moveBB);
            moveBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            byte sqFrom = (byte) (sqTo + offset);
            byte cPieceBB = pieceBitboardOnSquare(sqTo);
            byte cColorBB = getOpponentColor(colorBB);

            if (sqTo / 8 == promotionRow) {
                generatePromotionMoves(mvList, sqFrom, sqTo, pieceBB, colorBB, cPieceBB, cColorBB);
            } else {
                Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB, cPieceBB, cColorBB);
                if (checkKingSafety(mv)) {
                    mvList.addMove(mv);
                }
            }
        }
    }

    public void generatePromotionMoves(MoveList mvList, byte sqFrom, byte sqTo,
                                       byte pieceBB, byte colorBB, byte cPiece, byte cColor) {
        Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, queens);
        if (!checkKingSafety(mv)) return;
        mvList.addMove(mv);
        mvList.addMove(new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, rooks));
        mvList.addMove(new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, knights));
        mvList.addMove(new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, bishops));
    }

    public void generateEnPassantMoves(MoveList mvList, int eastOffset, int westOffset,
                                       int enPassantOffset, long maskEast, long maskWest, byte colorBB) {
        if (enPassantSquare == -1) return;

        byte enemyPawnSquare = (byte) (enPassantSquare + enPassantOffset);
        Move mv;

        byte eastSquare = (byte) (enPassantSquare + eastOffset);
        long eastSquareBB = (0x1L << eastSquare) & piecesBB[pawns];
        if (((eastSquareBB & piecesBB[colorBB]) != 0) && ((eastSquareBB & maskEast) != 0)) {
            mv = new Move(eastSquare, enPassantSquare, pawns, colorBB, pawns,
                    getOpponentColor(colorBB), (byte) 0, enemyPawnSquare);
            if (checkKingSafety(mv)) {
                mvList.addMove(mv);
            }
        }

        byte westSquare = (byte) (enPassantSquare + westOffset);
        long westSquareBB = (0x1L << westSquare) & piecesBB[pawns];
        if (((westSquareBB & piecesBB[colorBB]) != 0) && (westSquareBB & maskWest) != 0){
            mv = new Move(westSquare, enPassantSquare, pawns, colorBB, pawns,
                    getOpponentColor(colorBB), (byte) 0, enemyPawnSquare);
            if (checkKingSafety(mv)) {
                mvList.addMove(mv);
            }
        }
    }


    /* ==== Pawns moves ==== */

    public void whitePawnMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[pawns] & piecesBB[whitePieces]; // param
        long singlePush = (piecesBitboard << 8) & empty; // north
        long doublePush = ((singlePush & Square.RANK_3) << 8) & empty;
        long captureLeft = (piecesBitboard & Square.NOT_A_FILE) << 7 & piecesBB[blackPieces]; // north-west
        long captureRight = (piecesBitboard & Square.NOT_H_FILE) << 9 & piecesBB[blackPieces]; // north-east

        generatePawnMoves(mvList, singlePush, -8, 7, pawns, whitePieces);
        generatePawnMoves(mvList, doublePush, -16, 7, pawns, whitePieces);
        generatePawnCaptures(mvList, captureLeft, -7, 7, pawns, whitePieces);
        generatePawnCaptures(mvList, captureRight, -9, 7, pawns, whitePieces);
        generateEnPassantMoves(mvList, -7, -9, -8,
                Square.NOT_A_FILE, Square.NOT_H_FILE, whitePieces);
    }

    public void blackPawnMoves(MoveList mvList) {
        long piecesBitboard = piecesBB[pawns] & piecesBB[blackPieces]; // param
        long singlePush = (piecesBitboard >> 8) & empty; // south
        long doublePush = ((singlePush & Square.RANK_6) >> 8) & empty;
        long captureLeft = (piecesBitboard & Square.NOT_H_FILE) >> 7 & piecesBB[whitePieces];  // south-west
        long captureRight = (piecesBitboard & Square.NOT_A_FILE) >> 9 & piecesBB[whitePieces]; // south-east

        generatePawnMoves(mvList, singlePush, 8, 0, pawns, blackPieces);
        generatePawnMoves(mvList, doublePush, 16, 0, pawns, blackPieces);
        generatePawnCaptures(mvList, captureLeft, 7, 0, pawns, blackPieces);
        generatePawnCaptures(mvList, captureRight, 9, 0, pawns, blackPieces);
        generateEnPassantMoves(mvList, 7, 9, 8,
                Square.NOT_H_FILE, Square.NOT_A_FILE, blackPieces);
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, knights, whitePieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, knights, whitePieces);
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, knights, blackPieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, knights, blackPieces);
        }
    }

    /* ====Kings moves ==== */

    public void whiteKingMoves(MoveList mvList, int check) {
        byte whiteKingSquare = BinaryHelper.bitScanForward(piecesBB[whiteKing]);
        byte blackKingSquare = BinaryHelper.bitScanForward(piecesBB[blackKing]);

        long possibleAttackSquares = Piece.kingAttacks(whiteKingSquare) & ~Piece.kingAttacks(blackKingSquare);
        long moveBitboard = possibleAttackSquares & empty;
        long takeBitboard = possibleAttackSquares & piecesBB[blackPieces];

        generatePieceMoves(mvList, moveBitboard, whiteKingSquare, whiteKing, whitePieces);
        generatePieceCaptures(mvList, takeBitboard, whiteKingSquare, whiteKing, whitePieces);
        generateWhiteShortCastling(mvList, check);
        generateWhiteLongCastling(mvList, check);
    }

    public void blackKingMoves(MoveList mvList, int check) {
        byte whiteKingSquare = BinaryHelper.bitScanForward(piecesBB[whiteKing]);
        byte blackKingSquare = BinaryHelper.bitScanForward(piecesBB[blackKing]);

        long possibleAttackSquares = Piece.kingAttacks(blackKingSquare) & ~Piece.kingAttacks(whiteKingSquare);
        long moveBitboard = possibleAttackSquares & empty;
        long takeBitboard = possibleAttackSquares & piecesBB[whitePieces];

        generatePieceMoves(mvList, moveBitboard, blackKingSquare, blackKing, blackPieces);
        generatePieceCaptures(mvList, takeBitboard, blackKingSquare, blackKing, blackPieces);
        generateBlackShortCastling(mvList, check);
        generateBlackLongCastling(mvList, check);
    }

    private void generateBlackShortCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedBlackShortCastle
                && Square.isEmpty(Square.F8, occupied)
                && Square.isEmpty(Square.G8, occupied)
                && (!isSquareAttacked(Square.F8, whitePieces))
                && (!isSquareAttacked(Square.G8, whitePieces))) {
            Move mv = new Move(Square.E8, Square.G8, blackKing, blackPieces);
            mvList.addMove(mv);
        }
    }

    private void generateBlackLongCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedBlackLongCastle
                && Square.isEmpty(Square.B8, occupied)
                && Square.isEmpty(Square.C8, occupied)
                && Square.isEmpty(Square.D8, occupied)
                && (!isSquareAttacked(Square.C8, whitePieces))
                && (!isSquareAttacked(Square.D8, whitePieces))) {
            Move mv = new Move(Square.E8, Square.C8, blackKing, blackPieces);
            mvList.addMove(mv);
        }
    }

    private void generateWhiteShortCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedWhiteShortCastle
                && Square.isEmpty(Square.F1, occupied)
                && Square.isEmpty(Square.G1, occupied)
                && (!isSquareAttacked(Square.F1, blackPieces))
                && (!isSquareAttacked(Square.G1, blackPieces))) {
            Move mv = new Move(Square.E1, Square.G1, whiteKing, whitePieces);
            mvList.addMove(mv);
        }
    }

    private void generateWhiteLongCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedWhiteLongCastle
                && Square.isEmpty(Square.B1, occupied)
                && Square.isEmpty(Square.C1, occupied)
                && Square.isEmpty(Square.D1, occupied)
                && (!isSquareAttacked(Square.C1, blackPieces))
                && (!isSquareAttacked(Square.D1, blackPieces))) {
            Move mv = new Move(Square.E1, Square.C1, whiteKing, whitePieces);
            mvList.addMove(mv);
        }
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, bishops, whitePieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, bishops, whitePieces);
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, bishops, blackPieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, bishops, blackPieces);
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, rooks, whitePieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, rooks, whitePieces);
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, rooks, blackPieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, rooks, blackPieces);
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, queens, whitePieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, queens, whitePieces);
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

            generatePieceMoves(mvList, moveBitboard, sqFrom, queens, blackPieces);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, queens, blackPieces);
        }
    }

    /* ==== Legal moves ==== */

    public MoveList whitesLegalMoves() {
        MoveList whitesLegalMoves = new MoveList();

        int count = checkCount(getKingSquare(whitePieces), blackPieces);
        whiteKingMoves(whitesLegalMoves, count);
        if (count > 1) return whitesLegalMoves;

        whitePawnMoves(whitesLegalMoves);
        whiteKnightMoves(whitesLegalMoves);
        whiteBishopMoves(whitesLegalMoves);
        whiteRookMoves(whitesLegalMoves);
        whiteQueenMoves(whitesLegalMoves);

        return whitesLegalMoves;
    }

    public MoveList blacksLegalMoves() {
        MoveList blacksLegalMoves = new MoveList();

        int count = checkCount(getKingSquare(blackPieces), whitePieces);
        blackKingMoves(blacksLegalMoves, count);
        if (count > 1) return blacksLegalMoves;

        blackPawnMoves(blacksLegalMoves);
        blackKnightMoves(blacksLegalMoves);
        blackBishopMoves(blacksLegalMoves);
        blackRookMoves(blacksLegalMoves);
        blackQueenMoves(blacksLegalMoves);

        return blacksLegalMoves;
    }

    public MoveList generateLegalMoves() {
        return isWhiteSideToPlay ? whitesLegalMoves() : blacksLegalMoves();
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
        else if ((bbSquare & piecesBB[whiteKing]) != 0) return Piece.WHITE_KING;

        else if ((piecesBB[whitePieces] & bbSquare) != 0) {
            if ((piecesBB[pawns] & bbSquare) != 0) return Piece.WHITE_PAWN;
            else if ((piecesBB[knights] & bbSquare) != 0) return Piece.WHITE_KNIGHT;
            else if ((piecesBB[bishops] & bbSquare) != 0) return Piece.WHITE_BISHOP;
            else if ((piecesBB[rooks] & bbSquare) != 0) return Piece.WHITE_ROOK;
            else if ((piecesBB[queens] & bbSquare) != 0) return Piece.WHITE_QUEEN;
            return Piece.NONE;
        }

        else if ((piecesBB[blackPieces] & bbSquare) != 0) {
            if ((piecesBB[pawns] & bbSquare) != 0) return Piece.BLACK_PAWN;
            else if ((piecesBB[knights] & bbSquare) != 0) return Piece.BLACK_KNIGHT;
            else if ((piecesBB[bishops] & bbSquare) != 0) return Piece.BLACK_BISHOP;
            else if ((piecesBB[rooks] & bbSquare) != 0) return Piece.BLACK_ROOK;
            else if ((piecesBB[queens] & bbSquare) != 0) return Piece.BLACK_QUEEN;
        }

        return Piece.NONE;
    }

    private boolean isBlackShortCastling(Move move) {
        return move.getFrom() == Square.E8 && move.getTo() == Square.G8 && move.getPiece() == blackKing;
    }

    private boolean isBlackLongCastling(Move move) {
        return move.getFrom() == Square.E8 && move.getTo() == Square.C8 && move.getPiece() == blackKing;
    }

    private boolean isWhiteShortCastling(Move move) {
        return move.getFrom() == Square.E1 && move.getTo() == Square.G1 && move.getPiece() == whiteKing;
    }

    private boolean isWhiteLongCastling(Move move) {
        return move.getFrom() == Square.E1 && move.getTo() == Square.C1 && move.getPiece() == whiteKing;
    }

    /* ################### MAKEMOVE AND UNMAKEMOVE ################### */

    private void playBlackCastlingBitboardOnly(boolean isShort) {
        long kingFrom = Square.bitboardForSquare(Square.E8);
        long kingTo = Square.bitboardForSquare(isShort ? Square.G8 : Square.C8);
        long rookFrom = Square.bitboardForSquare(isShort ? Square.H8 : Square.A8);
        long rookTo = Square.bitboardForSquare(isShort ? Square.F8 : Square.D8);
        long kingFromToBB = kingFrom ^ kingTo;
        long rookFromToBB = rookFrom ^ rookTo;

        occupied ^= kingFromToBB ^ rookFromToBB;
        empty ^= kingFromToBB ^ rookFromToBB;
        piecesBB[blackPieces] ^= kingFromToBB ^ rookFromToBB;
        piecesBB[rooks] ^= rookFromToBB;
        piecesBB[blackKing] ^= kingFromToBB;
    }

    private void playWhiteCastlingBitboardOnly(boolean isShort) {
        long kingFrom = Square.bitboardForSquare(Square.E1);
        long kingTo = Square.bitboardForSquare(isShort ? Square.G1 : Square.C1);
        long rookFrom = Square.bitboardForSquare(isShort ? Square.H1 : Square.A1);
        long rookTo = Square.bitboardForSquare(isShort ? Square.F1 : Square.D1);
        long kingFromToBB = kingFrom ^ kingTo;
        long rookFromToBB = rookFrom ^ rookTo;

        occupied ^= kingFromToBB ^ rookFromToBB;
        empty ^= kingFromToBB ^ rookFromToBB;
        piecesBB[whitePieces] ^= kingFromToBB ^ rookFromToBB;
        piecesBB[rooks] ^= rookFromToBB;
        piecesBB[whiteKing] ^= kingFromToBB;
    }

    private void updateWhiteCastlingRights(Move move) {
        // Rook captured
        if (move.getCapturedPiece() == rooks) {
            switch (move.getTo()) {
                case Square.A8: isAllowedBlackLongCastle = false; break;
                case Square.H8: isAllowedBlackShortCastle = false; break;
            }
        }

        // Rook moved
        if (move.getPiece() == rooks) {
            switch (move.getFrom()) {
                case Square.A1: isAllowedWhiteLongCastle = false; break;
                case Square.H1: isAllowedWhiteShortCastle = false; break;
            }
        }

        // King moved
        if (move.getPiece() == whiteKing) {
            isAllowedWhiteLongCastle = false;
            isAllowedWhiteShortCastle = false;
        }
    }

    private void updateBlackCastlingRights(Move move) {
        // Rook captured
        if (move.getCapturedPiece() == rooks) {
            switch (move.getTo()) {
                case Square.A1: isAllowedWhiteLongCastle = false; break;
                case Square.H1: isAllowedWhiteShortCastle = false; break;
            }
        }

        // Rook moved
        if (move.getPiece() == rooks) {
            switch (move.getFrom()) {
                case Square.A8: isAllowedBlackLongCastle = false; break;
                case Square.H8: isAllowedBlackShortCastle = false; break;
            }
        }

        // King moved
        if (move.getPiece() == blackKing) {
            isAllowedBlackLongCastle = false;
            isAllowedBlackShortCastle = false;
        }
    }

    public void makeMove(Move move) {
        moveStateHistory.push(new MoveState(this));
        if (isWhiteSideToPlay) makeWhiteMove(move); else makeBlackMove(move);

        if (move.getPiece() == pawns && move.isDoublePawnPush()) {
            enPassantSquare = (byte) ((move.getFrom() + move.getTo()) / 2);
        } else {
            enPassantSquare = -1;
        }

        isWhiteSideToPlay = !isWhiteSideToPlay;
    }

    public void makeWhiteMoveBitboardOnly(Move move) {
        if (isWhiteShortCastling(move)) {playWhiteCastlingBitboardOnly(true); return;}
        if (isWhiteLongCastling(move)) {playWhiteCastlingBitboardOnly(false); return;}

        long fromBB = 0x1L << move.getFrom();
        long toBB = 0x1L << move.getTo();
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            if (move.isEnPassant()) {
                long epBB = 0x1L << move.getEnPassant();
                piecesBB[pawns] ^= epBB;
                piecesBB[blackPieces] ^= epBB;
                occupied ^= epBB ^ fromToBB;
                empty ^= epBB ^ fromToBB;
            } else {
                piecesBB[move.getCapturedPiece()] ^= toBB;
                piecesBB[move.getCapturedColor()] ^= toBB;
                occupied ^= fromBB;
                empty ^= fromBB;
            }
        } else {
            occupied ^= fromToBB;
            empty ^= fromToBB;
        }

        if (move.isPromotion()) {
            piecesBB[pawns] ^= fromBB;
            piecesBB[move.getPromotedPiece()] ^= toBB;
        } else {
            piecesBB[move.getPiece()] ^= fromToBB;
        }

        piecesBB[move.getColor()] ^= fromToBB;
    }

    public void makeWhiteMove(Move move) {
        makeWhiteMoveBitboardOnly(move);

        if (isWhiteShortCastling(move) || isWhiteLongCastling(move)) {
            isAllowedWhiteShortCastle = false;
            isAllowedWhiteLongCastle = false;
        } else {
            updateWhiteCastlingRights(move);
        }
    }

    public void makeBlackMoveBitboardOnly(Move move) {
        if (isBlackShortCastling(move)) {playBlackCastlingBitboardOnly(true);return;}
        if (isBlackLongCastling(move)) {playBlackCastlingBitboardOnly(false);return;}

        long fromBB = 0x1L << move.getFrom();
        long toBB = 0x1L << move.getTo();
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            if (move.isEnPassant()) {
                long epBB = 0x1L << move.getEnPassant();
                piecesBB[pawns] ^= epBB;
                piecesBB[whitePieces] ^= epBB;
                occupied ^= epBB ^ fromToBB;
                empty ^= epBB ^ fromToBB;
            } else {
                piecesBB[move.getCapturedPiece()] ^= toBB;
                piecesBB[move.getCapturedColor()] ^= toBB;
                occupied ^= fromBB;
                empty ^= fromBB;
            }
        } else {
            occupied ^= fromToBB;
            empty ^= fromToBB;
        }

        if (move.isPromotion()) {
            piecesBB[pawns] ^= fromBB;
            piecesBB[move.getPromotedPiece()] ^= toBB;
        } else {
            piecesBB[move.getPiece()] ^= fromToBB;
        }

        piecesBB[move.getColor()] ^= fromToBB;
    }

    public void makeBlackMove(Move move) {
        makeBlackMoveBitboardOnly(move);

        if (isBlackShortCastling(move) || isBlackLongCastling(move)) {
            isAllowedBlackShortCastle = false;
            isAllowedBlackLongCastle = false;
        } else {
            updateBlackCastlingRights(move);
        }
    }

    private void undoBlackCastling(boolean isShort) {
        long kingTo = Square.bitboardForSquare(Square.E8);
        long kingFrom = Square.bitboardForSquare(isShort ? Square.G8 : Square.C8);
        long rookTo = Square.bitboardForSquare(isShort ? Square.H8 : Square.A8);
        long rookFrom = Square.bitboardForSquare(isShort ? Square.F8 : Square.D8);
        long kingFromToBB = kingFrom ^ kingTo;
        long rookFromToBB = rookFrom ^ rookTo;

        occupied ^= kingFromToBB ^ rookFromToBB;
        empty ^= kingFromToBB ^ rookFromToBB;
        piecesBB[blackPieces] ^= kingFromToBB ^ rookFromToBB;
        piecesBB[rooks] ^= rookFromToBB;
        piecesBB[blackKing] ^= kingFromToBB;
    }

    private void undoWhiteCastling(boolean isShort) {
        long kingTo = Square.bitboardForSquare(Square.E1);
        long kingFrom = Square.bitboardForSquare(isShort ? Square.G1 : Square.C1);
        long rookTo = Square.bitboardForSquare(isShort ? Square.H1 : Square.A1);
        long rookFrom = Square.bitboardForSquare(isShort ? Square.F1 : Square.D1);
        long kingFromToBB = kingFrom ^ kingTo;
        long rookFromToBB = rookFrom ^ rookTo;

        occupied ^= kingFromToBB ^ rookFromToBB;
        empty ^= kingFromToBB ^ rookFromToBB;
        piecesBB[whitePieces] ^= kingFromToBB ^ rookFromToBB;
        piecesBB[rooks] ^= rookFromToBB;
        piecesBB[whiteKing] ^= kingFromToBB;
    }

    private MoveState restaureMoveState() {
        MoveState previousState = null;
        if (!moveStateHistory.isEmpty()) {
            previousState = moveStateHistory.pop();
            this.isAllowedBlackShortCastle = previousState.isAllowedBlackShortCastle;
            this.isAllowedBlackLongCastle = previousState.isAllowedBlackLongCastle;
            this.isAllowedWhiteShortCastle = previousState.isAllowedWhiteShortCastle;
            this.isAllowedWhiteLongCastle = previousState.isAllowedWhiteLongCastle;
            this.isWhiteSideToPlay = previousState.isWhiteSideToPlay;
            this.enPassantSquare = previousState.enPassantSquare;
        }
        return previousState;
    }

    public void unmakeMove(Move move) {
        if (!isWhiteSideToPlay) unmakeMoveWhite(move); else unmakeMoveBlack(move);
        restaureMoveState();
    }

    public void unmakeMoveWhite(Move move) {
        if (isWhiteShortCastling(move)) {undoWhiteCastling(true); return;}
        if (isWhiteLongCastling(move)) {undoWhiteCastling(false); return;}

        long fromBB = 0x1L << move.getFrom();
        long toBB = 0x1L << move.getTo();
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            if (move.isEnPassant()) {
                long epBB = 0x1L << move.getEnPassant();
                piecesBB[pawns] ^= epBB;
                piecesBB[blackPieces] ^= epBB;
                occupied ^= epBB ^ fromToBB;
                empty ^= epBB ^ fromToBB;
            } else {
                piecesBB[move.getCapturedPiece()] ^= toBB;
                piecesBB[move.getCapturedColor()] ^= toBB;
                occupied ^= fromBB;
                empty ^= fromBB;
            }
        } else {
            occupied ^= fromToBB;
            empty ^= fromToBB;
        }

        if (move.isPromotion()) {
            piecesBB[pawns] ^= fromBB;
            piecesBB[move.getPromotedPiece()] ^= toBB;
        } else {
            piecesBB[move.getPiece()] ^= fromToBB;
        }

        piecesBB[move.getColor()] ^= fromToBB;
    }

    public void unmakeMoveBlack(Move move) {
        if (isBlackShortCastling(move)) {undoBlackCastling(true); return;}
        if (isBlackLongCastling(move)) {undoBlackCastling(false); return;}

        long fromBB = 0x1L << move.getFrom();
        long toBB = 0x1L << move.getTo();
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            if (move.isEnPassant()) {
                long epBB = 0x1L << move.getEnPassant();
                piecesBB[pawns] ^= epBB;
                piecesBB[whitePieces] ^= epBB;
                occupied ^= epBB ^ fromToBB;
                empty ^= epBB ^ fromToBB;
            } else {
                piecesBB[move.getCapturedPiece()] ^= toBB;
                piecesBB[move.getCapturedColor()] ^= toBB;
                occupied ^= fromBB;
                empty ^= fromBB;
            }
        } else {
            occupied ^= fromToBB;
            empty ^= fromToBB;
        }

        if (move.isPromotion()) {
            piecesBB[pawns] ^= fromBB;
            piecesBB[move.getPromotedPiece()] ^= toBB;
        } else {
            piecesBB[move.getPiece()] ^= fromToBB;
        }

        piecesBB[move.getColor()] ^= fromToBB;
    }

    public static Position copy(Position pos) {
        Position newPos = new Position();
        newPos.occupied = pos.occupied;
        newPos.empty = pos.empty;
        newPos.isWhiteSideToPlay = pos.isWhiteSideToPlay;
        newPos.enPassantSquare = pos.enPassantSquare;
        newPos.isAllowedWhiteShortCastle = pos.isAllowedWhiteShortCastle;
        newPos.isAllowedWhiteLongCastle = pos.isAllowedWhiteLongCastle;
        newPos.isAllowedBlackShortCastle = pos.isAllowedBlackShortCastle;
        newPos.isAllowedBlackLongCastle = pos.isAllowedBlackLongCastle;

        newPos.piecesBB = pos.piecesBB.clone();

        newPos.moveStateHistory = new Stack<>();
        for (MoveState ms : pos.moveStateHistory) {
            newPos.moveStateHistory.push(new MoveState(ms));
        }

        return newPos;
    }
}
