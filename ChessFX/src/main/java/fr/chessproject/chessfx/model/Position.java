package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.helpers.BitboardUtilities;

import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Position {

    /* Bitboards indexes */

    public final byte kings = PieceIndex.KINGS.id;
    public final byte queens = PieceIndex.QUEENS.id;
    public final byte bishops = PieceIndex.BISHOPS.id;
    public final byte knights = PieceIndex.KNIGHTS.id;
    public final byte rooks = PieceIndex.ROOKS.id;
    public final byte pawns = PieceIndex.PAWNS.id;
    public final byte whitePieces = PieceIndex.WHITE_PIECES.id;
    public final byte blackPieces = PieceIndex.BLACK_PIECES.id;

    public int castlingRights;

    public boolean isWhiteSideToPlay;
    public byte enPassantSquare;
    public int halfMoveClock;
    public int fullMoveCounter;

    public long[] piecesBB;
    public long occupied;
    public long empty;
    private Stack<MoveState> moveStateHistory;
    private final AttackInfo attackInfo;
    public long hash;

    public Position() {
        reset();
        attackInfo = new AttackInfo(this);
    }

    public void reset() {
        this.piecesBB = new long[8];
        occupied = empty = 0x0L;
        isWhiteSideToPlay = true;
        enPassantSquare = -1;
        halfMoveClock = 0;
        fullMoveCounter = 1;
        castlingRights = 0;
        moveStateHistory = new Stack<>();
    }

    public long getOccupied() {
        return occupied;
    }

    public byte getKingSquare(byte color) {
        long kingBB = piecesBB[kings] & piecesBB[color];
        return (byte) Long.numberOfTrailingZeros(kingBB);
    }

    public byte getFriendlyColor() {
        return isWhiteSideToPlay ? whitePieces : blackPieces;
    }

    public byte getOpponentColor() {
        return isWhiteSideToPlay ? blackPieces : whitePieces;
    }

    public byte getEpSquare() {
        return enPassantSquare;
    }

    public long getHash() {
        return hash;
    }

    public boolean isInCheck() {
        return Long.bitCount(attackInfo.attackers) > 0;
    }

    public boolean isPinned(byte piece) {
        return (attackInfo.pinned & (1L << piece)) != 0;
    }

    public boolean isFriendly(byte sq) {
        long bbSquare = Square.bitboardForSquare(sq);
        return ((piecesBB[whitePieces] & bbSquare) != 0) && isWhiteSideToPlay ||
                ((piecesBB[blackPieces] & bbSquare) != 0) && !isWhiteSideToPlay;
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    public boolean isInsufficientMaterial() {
        return (occupied == piecesBB[kings]) ||
                (occupied == (piecesBB[kings] | piecesBB[knights]) && Long.bitCount(piecesBB[knights]) == 1) ||
                (occupied == (piecesBB[kings] | piecesBB[bishops]) && Long.bitCount(piecesBB[bishops]) == 1) ||
                (occupied == (piecesBB[kings] | piecesBB[bishops]) && Long.bitCount(piecesBB[bishops]) == 2 &&
                        BitboardUtilities.areSameColorsBishops(piecesBB[bishops]));
    }


    /* ================ FEN ================ */

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

        hash = Zobrist.computeHash(this);
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
                PieceType piece = PieceType.fromId(Piece.fromChar(c));

                if (piece == PieceType.NONE) {
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

    public void addPieceToBitboard(PieceType piece, long pos) {
        if (Piece.isPawn(piece)) piecesBB[pawns] |= pos;
        else if (Piece.isKnight(piece)) piecesBB[knights] |= pos;
        else if (Piece.isBishop(piece)) piecesBB[bishops] |= pos;
        else if (Piece.isRook(piece)) piecesBB[rooks] |= pos;
        else if (Piece.isQueen(piece)) piecesBB[queens] |= pos;
        else if (Piece.isKing(piece)) piecesBB[kings] |= pos;
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
                setWhiteShortCastle();
            } else if (c == 'Q') {
                setWhiteLongCastle();
            } else if (c == 'k') {
                setBlackShortCastle();
            } else if (c == 'q') {
                setBlackLongCastle();
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
        if ((piecesBB[kings] & pos) != 0) return (piecesBB[whitePieces] & pos) != 0 ? 'K' : 'k';
        return ' ';
    }

    private String getCastlingFEN() {
        StringBuilder sb = new StringBuilder();
        if (isAllowedWhiteShortCastle()) sb.append('K');
        if (isAllowedWhiteLongCastle()) sb.append('Q');
        if (isAllowedBlackShortCastle()) sb.append('k');
        if (isAllowedBlackLongCastle()) sb.append('q');
        return (sb.isEmpty()) ? "-" : sb.toString();
    }

    private String getEpFEN() {
        if (enPassantSquare == -1) return "-";
        int file = enPassantSquare % 8;
        int rank = enPassantSquare / 8;
        return (char) ('a' + file) + Integer.toString(rank + 1);
    }

    /* ===================  SQUAREDATTACK =================== */

    public boolean isSquareAttacked(byte sq, byte opColor) {
        long knightsSq = piecesBB[opColor] & piecesBB[knights];
        if ((knightsSq & Piece.knightAttacks(sq)) != 0) return true;
        long pawnsSq = piecesBB[opColor] & piecesBB[pawns];
        if ((pawnsSq & (opColor == blackPieces ? Piece.whitePawnAttacks(sq) : Piece.blackPawnAttacks(sq))) != 0) return true;
        if (isSquaredAttackBySliders(sq, opColor)) return true;

        return (piecesBB[kings] & (opColor == whitePieces ?  piecesBB[whitePieces] : piecesBB[blackPieces]) & Piece.kingAttacks(sq)) != 0;
    }

    public boolean isSquaredAttackBySliders(byte sq, byte opColor) {
        long rooksSq = piecesBB[opColor] & piecesBB[rooks];
        if ((rooksSq & Piece.rookAttacksLookup(occupied, sq)) != 0) return true;
        long bishopsSq = piecesBB[opColor] & piecesBB[bishops];
        if ((bishopsSq & Piece.bishopAttacksLookup(occupied, sq)) != 0) return true;
        long queensSq = piecesBB[opColor] & piecesBB[queens];
        return (queensSq & Piece.queenAttacksLookup(occupied, sq)) != 0;
    }

    /* ================== PIECES MOVES ================== */

    private void extractQuietMoves(MoveList mvList, byte sqFrom, byte pieceBB, byte colorBB, long legalMovesBB) {
        while(legalMovesBB != 0) {
            long piece = Long.lowestOneBit(legalMovesBB);
            legalMovesBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB);
            mvList.addMove(mv);
        }
    }

    private void extractCaptures(MoveList mvList, byte sqFrom, byte pieceBB, byte colorBB, long legalMovesBB) {
        while(legalMovesBB != 0) {
            long piece = Long.lowestOneBit(legalMovesBB);
            legalMovesBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB, pieceBitboardOnSquare(sqTo), getOpponentColor());
            mvList.addMove(mv);
        }
    }

    public void generatePieceMoves(MoveList mvList, long moveBB, byte sqFrom, byte pieceBB, byte colorBB) {
        long legalMovesBB = moveBB & attackInfo.checkRay & attackInfo.pinMasks[sqFrom];
        extractQuietMoves(mvList, sqFrom, pieceBB, colorBB, legalMovesBB);
    }

    public void generatePieceCaptures(MoveList mvList, long moveBB, byte sqFrom, byte pieceBB, byte colorBB) {
        long legalMovesBB = moveBB & attackInfo.checkRay & attackInfo.pinMasks[sqFrom];
        extractCaptures(mvList, sqFrom, pieceBB, colorBB, legalMovesBB);
    }

    public void generateKingMoves(MoveList mvList, long moveBB, byte sqFrom, byte pieceBB, byte colorBB) {
        long legalMovesBB = moveBB & ~attackInfo.enemyAttacks;
        extractQuietMoves(mvList, sqFrom, pieceBB, colorBB, legalMovesBB);
    }

    public void generateKingCaptures(MoveList mvList, long moveBB, byte sqFrom, byte pieceBB, byte colorBB) {
        long legalMovesBB = moveBB & ~attackInfo.enemyAttacks;
        extractCaptures(mvList, sqFrom, pieceBB, colorBB, legalMovesBB);
    }

    public void generatePawnMoves(MoveList mvList, long moveBB, int offset, int promotionRow, byte pieceBB, byte colorBB) {
        long legalMovesBB = moveBB & attackInfo.checkRay;

        while(legalMovesBB != 0) {
            long piece = Long.lowestOneBit(legalMovesBB);
            legalMovesBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            byte sqFrom = (byte) (sqTo + offset);

            if (((1L << sqTo) & (attackInfo.pinMasks[sqFrom])) == 0) continue;

            if (sqTo / 8 == promotionRow) {
                generatePromotionMoves(mvList, sqFrom, sqTo, pieceBB, colorBB, (byte) 0, (byte) 0);
            } else {
                Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB);
                mvList.addMove(mv);
            }
        }
    }

    public void generatePawnCaptures(MoveList mvList, long moveBB, int offset, int promotionRow, byte pieceBB, byte colorBB) {
        long legalMovesBB = moveBB & attackInfo.checkRay;

        while(legalMovesBB != 0) {
            long piece = Long.lowestOneBit(legalMovesBB);
            legalMovesBB &= ~piece;
            byte sqTo =  (byte)Long.numberOfTrailingZeros(piece);
            byte sqFrom = (byte) (sqTo + offset);
            byte cPieceBB = pieceBitboardOnSquare(sqTo);
            byte cColorBB = getOpponentColor();

            if (((1L << sqTo) & (attackInfo.pinMasks[sqFrom])) == 0) continue;

            if (sqTo / 8 == promotionRow) {
                generatePromotionMoves(mvList, sqFrom, sqTo, pieceBB, colorBB, cPieceBB, cColorBB);
            } else {
                Move mv = new Move(sqFrom, sqTo, pieceBB, colorBB, cPieceBB, cColorBB);
                mvList.addMove(mv);
            }
        }
    }

    public void generatePromotionMoves(MoveList mvList, byte sqFrom, byte sqTo,
                                       byte pieceBB, byte colorBB, byte cPiece, byte cColor) {
        mvList.addMove(new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, queens));
        mvList.addMove(new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, rooks));
        mvList.addMove(new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, knights));
        mvList.addMove(new Move(sqFrom, sqTo, pieceBB, colorBB, cPiece, cColor, bishops));
    }

    public void generateEnPassantMoves(MoveList mvList, int eastOffset, int westOffset,
                                       int enPassantOffset, long maskEast, long maskWest, byte colorBB) {
        if (enPassantSquare == -1) return;

        int[] directions = {eastOffset, westOffset};
        long[] masks = {maskEast, maskWest};
        byte enemySq = (byte) (enPassantSquare + enPassantOffset);

        for (int i = 0; i < 2; i++) {
            byte fromSq = (byte) (enPassantSquare + directions[i]);
            if (((1L << fromSq) & piecesBB[pawns] & piecesBB[colorBB] & masks[i]) == 0) continue;
            Move mv = new Move(fromSq, enPassantSquare, pawns, colorBB, pawns, getOpponentColor(), (byte) 0, enemySq);
            if (isEnPassantLegal(fromSq, enPassantSquare, enemySq, colorBB)) {
                mvList.addMove(mv);
            }
        }
    }

    public boolean isEnPassantLegal(byte fromSq, byte epSquare, byte enemySq, byte color) {
        long fromBB = 0x1L << fromSq;
        long toBB = 0x1L << epSquare;
        long enemyBB = 0x1L << enemySq;
        long fromToBB = fromBB ^ toBB;
        byte opColor = getOpponentColor();

        applyAndRestoreEnPassant(fromToBB, enemyBB, color, opColor);
        boolean isLegal = !isSquaredAttackBySliders(getKingSquare(color), opColor);
        applyAndRestoreEnPassant(fromToBB, enemyBB, color, opColor);

        return isLegal;
    }

    public void applyAndRestoreEnPassant(long fromToBB, long enemyBB, byte color, byte opColor) {
        piecesBB[opColor] ^= enemyBB;
        occupied ^= enemyBB ^ fromToBB;
        empty ^= enemyBB ^ fromToBB;
        piecesBB[pawns] ^= enemyBB ^ fromToBB;
        piecesBB[color] ^= fromToBB;
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
        long singlePush = (piecesBitboard >>> 8) & empty; // south
        long doublePush = ((singlePush & Square.RANK_6) >>> 8) & empty;
        long captureLeft = (piecesBitboard & Square.NOT_H_FILE) >>> 7 & piecesBB[whitePieces];  // south-west
        long captureRight = (piecesBitboard & Square.NOT_A_FILE) >>> 9 & piecesBB[whitePieces]; // south-east

        generatePawnMoves(mvList, singlePush, 8, 0, pawns, blackPieces);
        generatePawnMoves(mvList, doublePush, 16, 0, pawns, blackPieces);
        generatePawnCaptures(mvList, captureLeft, 7, 0, pawns, blackPieces);
        generatePawnCaptures(mvList, captureRight, 9, 0, pawns, blackPieces);
        generateEnPassantMoves(mvList, 7, 9, 8,
                Square.NOT_H_FILE, Square.NOT_A_FILE, blackPieces);
    }

    private void extractPiecesAttacks(MoveList mvList, byte friendlyColor, byte enemyColor, byte pieceType,
                                      BiFunction<Long, Byte, Long> attackFunction) {
        long piecesBitboard = piecesBB[pieceType] & piecesBB[friendlyColor];

        while (piecesBitboard != 0) {

            long piece = Long.lowestOneBit(piecesBitboard);
            piecesBitboard &= ~piece;
            byte sqFrom = (byte)Long.numberOfTrailingZeros(piece);

            long possibleAttackSquares = attackFunction.apply(occupied, sqFrom);
            long moveBitboard = possibleAttackSquares & empty;
            long takeBitboard = possibleAttackSquares & piecesBB[enemyColor];

            generatePieceMoves(mvList, moveBitboard, sqFrom, pieceType, friendlyColor);
            generatePieceCaptures(mvList, takeBitboard, sqFrom, pieceType, friendlyColor);
        }
    }

    /* ==== Knights moves ==== */

    public void whiteKnightMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, whitePieces, blackPieces, knights, (_, sq) -> Piece.knightAttacks(sq));
    }

    public void blackKnightMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, blackPieces, whitePieces, knights, (_, sq) -> Piece.knightAttacks(sq));
    }

    /* ====Kings moves ==== */

    private void extractKingAttacks(MoveList mvList, int check, byte friendlyColor, byte enemyColor,
                                    byte friendlyKingSq, byte enemyKingSq, byte pieceType,
                                    BiConsumer<MoveList, Integer> shortCastlingFunction, BiConsumer<MoveList, Integer> longCastlingFunction) {
        long possibleAttackSquares = Piece.kingAttacks(friendlyKingSq) & ~Piece.kingAttacks(enemyKingSq);
        long moveBitboard = possibleAttackSquares & empty;
        long takeBitboard = possibleAttackSquares & piecesBB[enemyColor];

        generateKingMoves(mvList, moveBitboard, friendlyKingSq, pieceType, friendlyColor);
        generateKingCaptures(mvList, takeBitboard, friendlyKingSq, pieceType, friendlyColor);
        shortCastlingFunction.accept(mvList, check);
        longCastlingFunction.accept(mvList, check);
    }

    public void whiteKingMoves(MoveList mvList, int check) {
        byte whiteKingSquare = BitboardUtilities.bitScanForward(piecesBB[kings] & piecesBB[whitePieces]);
        byte blackKingSquare = BitboardUtilities.bitScanForward(piecesBB[kings] & piecesBB[blackPieces]);
        extractKingAttacks(mvList, check, whitePieces, blackPieces, whiteKingSquare, blackKingSquare, kings,
                this::generateWhiteShortCastling, this::generateWhiteLongCastling
        );
    }

    public void blackKingMoves(MoveList mvList, int check) {
        byte whiteKingSquare = BitboardUtilities.bitScanForward(piecesBB[kings] & piecesBB[whitePieces]);
        byte blackKingSquare = BitboardUtilities.bitScanForward(piecesBB[kings] & piecesBB[blackPieces]);
        extractKingAttacks(mvList, check, blackPieces, whitePieces, blackKingSquare, whiteKingSquare, kings,
                this::generateBlackShortCastling, this::generateBlackLongCastling
        );
    }

    public boolean isAllowedWhiteShortCastle() {
        return (castlingRights & 1) != 0;
    }

    public boolean isAllowedWhiteLongCastle() {
        return (castlingRights & 2) != 0;
    }

    public boolean isAllowedBlackShortCastle() {
        return (castlingRights & 4) != 0;
    }

    public boolean isAllowedBlackLongCastle() {
        return (castlingRights & 8) != 0;
    }

    public void setCastlingZobristKey(int offset) {
        hash ^= Zobrist.getCastlingKey(castlingRights);
        castlingRights |= offset;
        hash ^= Zobrist.getCastlingKey(castlingRights);
    }

    public void unsetCastlingZobristKey(int offset) {
        hash ^= Zobrist.getCastlingKey(castlingRights);
        castlingRights &= ~offset;
        hash ^= Zobrist.getCastlingKey(castlingRights);
    }

    public void setWhiteShortCastle() {
        setCastlingZobristKey(1);
    }

    public void unsetWhiteShortCastle() {
        unsetCastlingZobristKey(1);
    }

    public void setWhiteLongCastle() {
        setCastlingZobristKey(2);
    }

    public void unsetWhiteLongCastle() {
        unsetCastlingZobristKey(2);
    }

    public void setBlackShortCastle() {
        setCastlingZobristKey(4);
    }

    public void unsetBlackShortCastle() {
        unsetCastlingZobristKey(4);
    }

    public void setBlackLongCastle() {
        setCastlingZobristKey(8);
    }

    public void unsetBlackLongCastle() {
        unsetCastlingZobristKey(8);
    }

    private void generateBlackShortCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedBlackShortCastle()
                && Square.isEmpty(Square.F8, occupied)
                && Square.isEmpty(Square.G8, occupied)
                && (!isSquareAttacked(Square.F8, whitePieces))
                && (!isSquareAttacked(Square.G8, whitePieces))) {
            Move mv = new Move(Square.E8, Square.G8, kings, blackPieces);
            mvList.addMove(mv);
        }
    }

    private void generateBlackLongCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedBlackLongCastle()
                && Square.isEmpty(Square.B8, occupied)
                && Square.isEmpty(Square.C8, occupied)
                && Square.isEmpty(Square.D8, occupied)
                && (!isSquareAttacked(Square.C8, whitePieces))
                && (!isSquareAttacked(Square.D8, whitePieces))) {
            Move mv = new Move(Square.E8, Square.C8, kings, blackPieces);
            mvList.addMove(mv);
        }
    }

    private void generateWhiteShortCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedWhiteShortCastle()
                && Square.isEmpty(Square.F1, occupied)
                && Square.isEmpty(Square.G1, occupied)
                && (!isSquareAttacked(Square.F1, blackPieces))
                && (!isSquareAttacked(Square.G1, blackPieces))) {
            Move mv = new Move(Square.E1, Square.G1, kings, whitePieces);
            mvList.addMove(mv);
        }
    }

    private void generateWhiteLongCastling(MoveList mvList, int check) {
        if (check == 0 && isAllowedWhiteLongCastle()
                && Square.isEmpty(Square.B1, occupied)
                && Square.isEmpty(Square.C1, occupied)
                && Square.isEmpty(Square.D1, occupied)
                && (!isSquareAttacked(Square.C1, blackPieces))
                && (!isSquareAttacked(Square.D1, blackPieces))) {
            Move mv = new Move(Square.E1, Square.C1, kings, whitePieces);
            mvList.addMove(mv);
        }
    }

    /* ==== Bishops moves ==== */

    public void whiteBishopMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, whitePieces, blackPieces, bishops, Piece::bishopAttacksLookup);
    }

    public void blackBishopMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, blackPieces, whitePieces, bishops, Piece::bishopAttacksLookup);
    }

    /* ==== Rooks moves ==== */

    public void whiteRookMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, whitePieces, blackPieces, rooks, Piece::rookAttacksLookup);
    }

    public void blackRookMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, blackPieces, whitePieces, rooks, Piece::rookAttacksLookup);
    }

    /* ==== Queens moves ==== */

    public void whiteQueenMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, whitePieces, blackPieces, queens, Piece::queenAttacksLookup);
    }

    public void blackQueenMoves(MoveList mvList) {
        extractPiecesAttacks(mvList, blackPieces, whitePieces, queens, Piece::queenAttacksLookup);
    }

    /* ==== Legal moves ==== */

    public MoveList whitesLegalMoves() {
        MoveList whitesLegalMoves = new MoveList();

        int count = Long.bitCount(attackInfo.attackers);
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

        int count = Long.bitCount(attackInfo.attackers);
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
        attackInfo.update(this);
        return isWhiteSideToPlay ? whitesLegalMoves() : blacksLegalMoves();
    }

    public byte pieceBitboardOnSquare(byte sq) {
        long bbSquare = Square.bitboardForSquare(sq);
        if ((piecesBB[pawns] & bbSquare) != 0) return pawns;
        if ((piecesBB[knights] & bbSquare) != 0) return knights;
        if ((piecesBB[bishops] & bbSquare) != 0) return bishops;
        if ((piecesBB[queens] & bbSquare) != 0) return queens;
        if ((piecesBB[rooks] & bbSquare) != 0) return rooks;
        if ((piecesBB[kings] & bbSquare) != 0) return kings;
        return -1;
    }

    public byte pieceColorOnSquare(byte sq) {
        long bbSquare = Square.bitboardForSquare(sq);
        if ((piecesBB[whitePieces] & bbSquare) != 0) return whitePieces;
        if ((piecesBB[blackPieces] & bbSquare) != 0) return blackPieces;
        return -1;
    }

    public PieceType pieceOnSquare(byte sq) {
        long bb = Square.bitboardForSquare(sq);

        boolean isWhite = (piecesBB[whitePieces] & bb) != 0;
        boolean isBlack = (piecesBB[blackPieces] & bb) != 0;

        if (!isWhite && !isBlack) return PieceType.NONE;

        if ((piecesBB[pawns] & bb) != 0) return isWhite ? PieceType.WHITE_PAWN : PieceType.BLACK_PAWN;
        if ((piecesBB[knights] & bb) != 0) return isWhite ? PieceType.WHITE_KNIGHT : PieceType.BLACK_KNIGHT;
        if ((piecesBB[bishops] & bb) != 0) return isWhite ? PieceType.WHITE_BISHOP : PieceType.BLACK_BISHOP;
        if ((piecesBB[rooks] & bb) != 0) return isWhite ? PieceType.WHITE_ROOK : PieceType.BLACK_ROOK;
        if ((piecesBB[queens] & bb) != 0) return isWhite ? PieceType.WHITE_QUEEN : PieceType.BLACK_QUEEN;
        if ((piecesBB[kings] & bb) != 0) return isWhite ? PieceType.WHITE_KING : PieceType.BLACK_KING;

        return PieceType.NONE;
    }

    private boolean isBlackShortCastling(Move move) {
        return move.getFrom() == Square.E8 && move.getTo() == Square.G8
                && move.getPiece() == kings && move.getColor() == blackPieces;
    }

    private boolean isBlackLongCastling(Move move) {
        return move.getFrom() == Square.E8 && move.getTo() == Square.C8
                && move.getPiece() == kings && move.getColor() == blackPieces;
    }

    private boolean isWhiteShortCastling(Move move) {
        return move.getFrom() == Square.E1 && move.getTo() == Square.G1
                && move.getPiece() == kings && move.getColor() == whitePieces;
    }

    private boolean isWhiteLongCastling(Move move) {
        return move.getFrom() == Square.E1 && move.getTo() == Square.C1
                && move.getPiece() == kings && move.getColor() == whitePieces;
    }

    /* ================== makeMove and unmakeMove ================== */

    private void playCastlingBitboardOnly(boolean isShort, byte friendlyColor, byte pieceType,
                                          byte E, byte G, byte C, byte H, byte A, byte F, byte D) {
        long kingFrom = Square.bitboardForSquare(E);
        long kingTo = Square.bitboardForSquare(isShort ? G : C);
        long rookFrom = Square.bitboardForSquare(isShort ? H : A);
        long rookTo = Square.bitboardForSquare(isShort ? F: D);
        long kingFromToBB = kingFrom ^ kingTo;
        long rookFromToBB = rookFrom ^ rookTo;

        occupied ^= kingFromToBB ^ rookFromToBB;
        empty ^= kingFromToBB ^ rookFromToBB;
        piecesBB[friendlyColor] ^= kingFromToBB ^ rookFromToBB;
        piecesBB[rooks] ^= rookFromToBB;
        piecesBB[pieceType] ^= kingFromToBB;

        hash ^= Zobrist.getPieceSquareKey(pieceType, friendlyColor, E);
        hash ^= Zobrist.getPieceSquareKey(pieceType, friendlyColor, isShort ? G : C);
        hash ^= Zobrist.getPieceSquareKey(rooks, friendlyColor, isShort ? H : A);
        hash ^= Zobrist.getPieceSquareKey(rooks, friendlyColor, isShort ? F : D);
    }

    private void playBlackCastlingBitboardOnly(boolean isShort) {
        playCastlingBitboardOnly(isShort, blackPieces, kings,
                Square.E8, Square.G8, Square.C8, Square.H8, Square.A8, Square.F8, Square.D8);
    }

    private void playWhiteCastlingBitboardOnly(boolean isShort) {
        playCastlingBitboardOnly(isShort, whitePieces, kings,
                Square.E1, Square.G1, Square.C1, Square.H1, Square.A1, Square.F1, Square.D1);
    }

    private void updateWhiteCastlingRights(Move move) {
        // Rook captured
        if (move.getCapturedPiece() == rooks) {
            switch (move.getTo()) {
                case Square.A8: unsetBlackLongCastle(); break;
                case Square.H8: unsetBlackShortCastle(); break;
            }
        }

        // Rook moved
        if (move.getPiece() == rooks) {
            switch (move.getFrom()) {
                case Square.A1: unsetWhiteLongCastle(); break;
                case Square.H1: unsetWhiteShortCastle(); break;
            }
        }

        // King moved
        if (move.getPiece() == kings) {
            unsetWhiteLongCastle();
            unsetWhiteShortCastle();
        }
    }

    private void updateBlackCastlingRights(Move move) {
        // Rook captured
        if (move.getCapturedPiece() == rooks) {
            switch (move.getTo()) {
                case Square.A1: unsetWhiteLongCastle(); break;
                case Square.H1: unsetWhiteShortCastle(); break;
            }
        }

        // Rook moved
        if (move.getPiece() == rooks) {
            switch (move.getFrom()) {
                case Square.A8: unsetBlackLongCastle(); break;
                case Square.H8: unsetBlackShortCastle(); break;
            }
        }

        // King moved
        if (move.getPiece() == kings) {
            unsetBlackLongCastle();
            unsetBlackShortCastle();
        }
    }

    public void makeMove(Move move) {
        moveStateHistory.push(new MoveState(this));
        if (enPassantSquare != -1) hash ^= Zobrist.getEnPassantKey(enPassantSquare);

        if (isWhiteSideToPlay) makeWhiteMove(move); else makeBlackMove(move);

        if (move.getPiece() == pawns || move.isCapture()) halfMoveClock = 0;
        if (move.getPiece() == pawns && move.isDoublePawnPush()) {
            enPassantSquare = (byte) ((move.getFrom() + move.getTo()) / 2);
        } else {
            enPassantSquare = -1;
        }

        if (enPassantSquare != -1) hash ^= Zobrist.getEnPassantKey(enPassantSquare);
        isWhiteSideToPlay = !isWhiteSideToPlay;
        hash ^= Zobrist.getSideToMoveKey();
        halfMoveClock++;
    }

    public void makeMoveBitboardOnly(Move move, byte enemyColor) {
        byte piece = move.getPiece();
        byte sqFrom = move.getFrom();
        byte sqTo = move.getTo();
        byte color = move.getColor();
        byte cPiece = move.getCapturedPiece();
        byte cColor = move.getCapturedColor();
        byte pPiece = move.getPromotedPiece();

        long fromBB = 0x1L << sqFrom;
        long toBB = 0x1L << sqTo;
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            if (move.isEnPassant()) {
                byte epSq = move.getEnPassant();

                long enemyBB = 0x1L << epSq;
                piecesBB[pawns] ^= enemyBB;
                piecesBB[enemyColor] ^= enemyBB;
                occupied ^= enemyBB ^ fromToBB;
                empty ^= enemyBB ^ fromToBB;

                hash ^= Zobrist.getPieceSquareKey(pawns, enemyColor, epSq);
            } else {
                piecesBB[cPiece] ^= toBB;
                piecesBB[cColor] ^= toBB;
                occupied ^= fromBB;
                empty ^= fromBB;

                hash ^= Zobrist.getPieceSquareKey(cPiece, cColor, sqTo);
            }
        } else {
            occupied ^= fromToBB;
            empty ^= fromToBB;
        }

        if (move.isPromotion()) {
            piecesBB[pawns] ^= fromBB;
            piecesBB[pPiece] ^= toBB;

            hash ^= Zobrist.getPieceSquareKey(pawns, color, sqFrom);
            hash ^= Zobrist.getPieceSquareKey(pPiece, color, sqTo);
        } else {
            piecesBB[piece] ^= fromToBB;

            hash ^= Zobrist.getPieceSquareKey(piece, color, sqFrom);
            hash ^= Zobrist.getPieceSquareKey(piece, color, sqTo);
        }

        piecesBB[color] ^= fromToBB;
    }

    public void makeWhiteMove(Move move) {
        if (isWhiteShortCastling(move)) {
            playWhiteCastlingBitboardOnly(true);
            unsetWhiteShortCastle();
            unsetWhiteLongCastle();
        } else if (isWhiteLongCastling(move)) {
            playWhiteCastlingBitboardOnly(false);
            unsetWhiteShortCastle();
            unsetWhiteLongCastle();
        } else {
            makeMoveBitboardOnly(move, blackPieces);
            updateWhiteCastlingRights(move);
        }
    }

    public void makeBlackMove(Move move) {
        if (isBlackShortCastling(move)) {
            playBlackCastlingBitboardOnly(true);
            unsetBlackShortCastle();
            unsetBlackLongCastle();
        } else if (isBlackLongCastling(move)) {
            playBlackCastlingBitboardOnly(false);
            unsetBlackShortCastle();
            unsetBlackLongCastle();
        } else {
            makeMoveBitboardOnly(move, whitePieces);
            updateBlackCastlingRights(move);
        }
    }

    private void undoBlackCastling(boolean isShort) {
        playBlackCastlingBitboardOnly(isShort);
    }

    private void undoWhiteCastling(boolean isShort) {
        playWhiteCastlingBitboardOnly(isShort);
    }

    private void restaureMoveState() {
        MoveState previousState;
        if (!moveStateHistory.isEmpty()) {
            previousState = moveStateHistory.pop();
            this.castlingRights = previousState.castlingRights;
            this.isWhiteSideToPlay = previousState.isWhiteSideToPlay;
            this.enPassantSquare = previousState.enPassantSquare;
            this.hash = previousState.hash;
        }
    }

    public void unmakeMove(Move move) {
        if (!isWhiteSideToPlay) unmakeMoveWhite(move); else unmakeMoveBlack(move);
        restaureMoveState();
    }

    public void unmakeMoveWhite(Move move) {
        if (isWhiteShortCastling(move)) {undoWhiteCastling(true); return;}
        if (isWhiteLongCastling(move)) {undoWhiteCastling(false); return;}

        makeMoveBitboardOnly(move, blackPieces);
    }

    public void unmakeMoveBlack(Move move) {
        if (isBlackShortCastling(move)) {undoBlackCastling(true); return;}
        if (isBlackLongCastling(move)) {undoBlackCastling(false); return;}

        makeMoveBitboardOnly(move, whitePieces);
    }

    public AttackInfo getAttackInfo() {
        return attackInfo;
    }

    public Position copy() {
        Position newPos = new Position();
        newPos.occupied = this.occupied;
        newPos.empty = this.empty;
        newPos.isWhiteSideToPlay = this.isWhiteSideToPlay;
        newPos.enPassantSquare = this.enPassantSquare;
        newPos.castlingRights = this.castlingRights;
        newPos.hash = this.hash;

        newPos.piecesBB = this.piecesBB.clone();

        newPos.moveStateHistory = new Stack<>();
        for (MoveState ms : this.moveStateHistory) {
            newPos.moveStateHistory.push(new MoveState(ms));
        }

        return newPos;
    }

    public void printBoard() {
        char[] piecesChars = " KQBNRPkqbnrp".toCharArray();

        for (int rank = 7; rank >= 0; rank--)  {
            System.out.println("+---+---+---+---+---+---+---+---+");
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                PieceType piece = pieceOnSquare((byte) square);

                System.out.print("| ");

                if (piece == PieceType.NONE) {
                    System.out.print("  ");
                } else {
                    System.out.print(piecesChars[piece.id] + " ");
                }

                if (file == 7) {
                    System.out.print("|  " + (rank + 1));
                }
            }
            System.out.print("\n");
        }
        System.out.println("+---+---+---+---+---+---+---+---+");
        System.out.println("  a   b   c   d   e   f   g   h");
    }
}
