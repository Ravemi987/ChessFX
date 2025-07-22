package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.helpers.BinaryHelper;

import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Position {

    /* Bitboards indexes */

    private final byte whitePieces = PieceIndex.WHITE_PIECES.id;
    private final byte blackPieces = PieceIndex.BLACK_PIECES.id;
    private final byte pawns = PieceIndex.PAWNS.id;
    private final byte knights = PieceIndex.KNIGHTS.id;
    private final byte bishops = PieceIndex.BISHOPS.id;
    private final byte rooks = PieceIndex.ROOKS.id;
    private final byte queens = PieceIndex.QUEENS.id;
    private final byte blackKing = PieceIndex.BLACK_KING.id;
    private final byte whiteKing = PieceIndex.WHITE_KING.id;

    public boolean isAllowedBlackShortCastle;
    public boolean isAllowedBlackLongCastle;
    public boolean isAllowedWhiteShortCastle;
    public boolean isAllowedWhiteLongCastle;

    public boolean isWhiteSideToPlay;
    public byte enPassantSquare;
    public int halfMoveClock;
    public int fullMoveCounter;

    public long[] piecesBB;
    public long occupied;
    public long empty;
    private Stack<MoveState> moveStateHistory;
    private final AttackInfo attackInfo;

    public Position() {
        reset();
        attackInfo = new AttackInfo(this);
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

    public byte getFriendlyColor() {
        return isWhiteSideToPlay ? whitePieces : blackPieces;
    }

    public byte getOpponentColor() {
        return isWhiteSideToPlay ? blackPieces : whitePieces;
    }

    public byte getEpSquare() {
        return enPassantSquare;
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
        else if (piece == PieceType.WHITE_KING) piecesBB[whiteKing] |= pos;
        else if (piece == PieceType.BLACK_KING) piecesBB[blackKing] |= pos;
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

    /* ###################  SQUAREDATTACK ################### */

    public boolean isSquareAttacked(byte sq, byte opColor) {
        long knightsSq = piecesBB[opColor] & piecesBB[knights];
        if ((knightsSq & Piece.knightAttacks(sq)) != 0) return true;
        long pawnsSq = piecesBB[opColor] & piecesBB[pawns];
        if ((pawnsSq & (opColor == 1 ? Piece.whitePawnAttacks(sq) : Piece.blackPawnAttacks(sq))) != 0) return true;
        if (isSquaredAttackBySliders(sq, opColor)) return true;

        return ((piecesBB[opColor == 0 ? whiteKing : blackKing] & Piece.kingAttacks(sq)) != 0);
    }

    public boolean isSquaredAttackBySliders(byte sq, byte opColor) {
        long rooksSq = piecesBB[opColor] & piecesBB[rooks];
        if ((rooksSq & Piece.rookAttacksLookup(occupied, sq)) != 0) return true;
        long bishopsSq = piecesBB[opColor] & piecesBB[bishops];
        if ((bishopsSq & Piece.bishopAttacksLookup(occupied, sq)) != 0) return true;
        long queensSq = piecesBB[opColor] & piecesBB[queens];
        return (queensSq & Piece.queenAttacksLookup(occupied, sq)) != 0;
    }

    /* ################### PIECES MOVES ################### */

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
        byte opColor = (byte) ((color + 1) % 2);

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
        byte whiteKingSquare = BinaryHelper.bitScanForward(piecesBB[whiteKing]);
        byte blackKingSquare = BinaryHelper.bitScanForward(piecesBB[blackKing]);
        extractKingAttacks(mvList, check, whitePieces, blackPieces, whiteKingSquare, blackKingSquare, whiteKing,
                this::generateWhiteShortCastling, this::generateWhiteLongCastling
        );
    }

    public void blackKingMoves(MoveList mvList, int check) {
        byte whiteKingSquare = BinaryHelper.bitScanForward(piecesBB[whiteKing]);
        byte blackKingSquare = BinaryHelper.bitScanForward(piecesBB[blackKing]);
        extractKingAttacks(mvList, check, blackPieces, whitePieces, blackKingSquare, whiteKingSquare, blackKing,
                this::generateBlackShortCastling, this::generateBlackLongCastling
        );
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

    public PieceType pieceOnSquare(byte sq) {
        long bbSquare = Square.bitboardForSquare(sq);

        if ((bbSquare & piecesBB[blackKing]) != 0) return PieceType.BLACK_KING;
        else if ((bbSquare & piecesBB[whiteKing]) != 0) return PieceType.WHITE_KING;

        else if ((piecesBB[whitePieces] & bbSquare) != 0) {
            if ((piecesBB[pawns] & bbSquare) != 0) return PieceType.WHITE_PAWN;
            else if ((piecesBB[knights] & bbSquare) != 0) return PieceType.WHITE_KNIGHT;
            else if ((piecesBB[bishops] & bbSquare) != 0) return PieceType.WHITE_BISHOP;
            else if ((piecesBB[rooks] & bbSquare) != 0) return PieceType.WHITE_ROOK;
            else if ((piecesBB[queens] & bbSquare) != 0) return PieceType.WHITE_QUEEN;
            return PieceType.NONE;
        }

        else if ((piecesBB[blackPieces] & bbSquare) != 0) {
            if ((piecesBB[pawns] & bbSquare) != 0) return PieceType.BLACK_PAWN;
            else if ((piecesBB[knights] & bbSquare) != 0) return PieceType.BLACK_KNIGHT;
            else if ((piecesBB[bishops] & bbSquare) != 0) return PieceType.BLACK_BISHOP;
            else if ((piecesBB[rooks] & bbSquare) != 0) return PieceType.BLACK_ROOK;
            else if ((piecesBB[queens] & bbSquare) != 0) return PieceType.BLACK_QUEEN;
        }

        return PieceType.NONE;
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
    }

    private void playBlackCastlingBitboardOnly(boolean isShort) {
        playCastlingBitboardOnly(isShort, blackPieces, blackKing,
                Square.E8, Square.G8, Square.C8, Square.H8, Square.A8, Square.F8, Square.D8);
    }

    private void playWhiteCastlingBitboardOnly(boolean isShort) {
        playCastlingBitboardOnly(isShort, whitePieces, whiteKing,
                Square.E1, Square.G1, Square.C1, Square.H1, Square.A1, Square.F1, Square.D1);
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

    public void makeMoveBitboardOnly(Move move, byte enemyColor) {
        long fromBB = 0x1L << move.getFrom();
        long toBB = 0x1L << move.getTo();
        long fromToBB = fromBB ^ toBB;

        if (move.isCapture()) {
            if (move.isEnPassant()) {
                long enemyBB = 0x1L << move.getEnPassant();
                piecesBB[pawns] ^= enemyBB;
                piecesBB[enemyColor] ^= enemyBB;
                occupied ^= enemyBB ^ fromToBB;
                empty ^= enemyBB ^ fromToBB;
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
        if (isWhiteShortCastling(move)) {
            playWhiteCastlingBitboardOnly(true);
            isAllowedWhiteShortCastle = false;
            isAllowedWhiteLongCastle = false;
        } else if (isWhiteLongCastling(move)) {
            playWhiteCastlingBitboardOnly(false);
            isAllowedWhiteShortCastle = false;
            isAllowedWhiteLongCastle = false;
        } else {
            makeMoveBitboardOnly(move, blackPieces);
            updateWhiteCastlingRights(move);
        }
    }

    public void makeBlackMove(Move move) {
        if (isBlackShortCastling(move)) {
            playBlackCastlingBitboardOnly(true);
            isAllowedBlackShortCastle = false;
            isAllowedBlackLongCastle = false;
        } else if (isBlackLongCastling(move)) {
            playBlackCastlingBitboardOnly(false);
            isAllowedBlackShortCastle = false;
            isAllowedBlackLongCastle = false;
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
            this.isAllowedBlackShortCastle = previousState.isAllowedBlackShortCastle;
            this.isAllowedBlackLongCastle = previousState.isAllowedBlackLongCastle;
            this.isAllowedWhiteShortCastle = previousState.isAllowedWhiteShortCastle;
            this.isAllowedWhiteLongCastle = previousState.isAllowedWhiteLongCastle;
            this.isWhiteSideToPlay = previousState.isWhiteSideToPlay;
            this.enPassantSquare = previousState.enPassantSquare;
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

    // Helpers

    public AttackInfo getAttackInfo() {
        return attackInfo;
    }
}
