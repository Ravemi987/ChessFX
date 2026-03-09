package fr.chessproject.chessfx.model.Board;

import fr.chessproject.chessfx.helpers.BitboardUtilities;

import java.util.Arrays;
import java.util.function.BiFunction;

public class AttackInfo {

    public long attackers;
    public long checkRay;
    public long enemyAttacks;
    public byte kingSquare;
    public long pinned;
    private byte opColor;
    private byte color;

    public long[] checkersByPiece = new long[8];
    public long[] pinMasks = new long[64];

    private static final int[] rookDirs = new int[]{Piece.north, Piece.south, Piece.east, Piece.west};
    private static final int[] bishopDirs = new int[]{Piece.noEa, Piece.soWe, Piece.noWe, Piece.soEa};
    private static final int[] queenDirs = new int[]{
            Piece.north, Piece.south, Piece.east, Piece.west,
            Piece.noEa, Piece.soWe, Piece.noWe, Piece.soEa
    };

    public AttackInfo(Position p) {
        reset(p);
    }

    public void reset(Position p) {
        enemyAttacks = 0L;
        attackers = 0L;
        checkRay = -1L;
        pinned = 0L;

        color = p.getFriendlyColor();
        kingSquare = p.getKingSquare(color);
        opColor = p .getOpponentColor();

        Arrays.fill(pinMasks, -1L);
        Arrays.fill(checkersByPiece, 0L);
    }

    private long getPawnsAttacksFromAll(Position p) {
        long bb = p.piecesBB[PieceIndex.PAWNS.id] & p.piecesBB[opColor];
        long attacks = opColor == p.whitePieces ? (bb & Square.NOT_A_FILE) << 7 | (bb & Square.NOT_H_FILE) << 9 :
                (bb & Square.NOT_H_FILE) >>> 7 | (bb & Square.NOT_A_FILE) >>> 9;
        if ((attacks & (1L << kingSquare)) != 0) {
            checkersByPiece[PieceIndex.PAWNS.id] = bb & (color == p.whitePieces ? Piece.whitePawnAttacks(kingSquare) : Piece.blackPawnAttacks(kingSquare));
            attackers |= checkersByPiece[PieceIndex.PAWNS.id];
        }
        return attacks;
    }

    public long getAttacksFromAll(Position p, byte pieceType, BiFunction<Long, Byte, Long> attackFunction) {
        long bb = p.piecesBB[pieceType] & p.piecesBB[opColor];
        long allAttacks = 0L;

        while (bb != 0) {
            long lsb = Long.lowestOneBit(bb);
            bb &= ~lsb;
            byte sqFrom = (byte) Long.numberOfTrailingZeros(lsb);
            long attacks = attackFunction.apply(p.occupied ^ (1L << kingSquare), sqFrom);
            allAttacks |= attacks;
            if ((attacks & (1L << kingSquare)) != 0) {
                attackers |= (1L << sqFrom);
                checkersByPiece[pieceType] |= (1L << sqFrom);
            }
        }

        return allAttacks;
    }

    public void calculateAllAttacks(Position p) {
        enemyAttacks |= getPawnsAttacksFromAll(p);
        enemyAttacks |= getAttacksFromAll(p, PieceIndex.KNIGHTS.id, (_, sq) -> Piece.knightAttacks(sq));
        enemyAttacks |= getAttacksFromAll(p, PieceIndex.BISHOPS.id, Piece::bishopAttacksLookup);
        enemyAttacks |= getAttacksFromAll(p, PieceIndex.ROOKS.id, Piece::rookAttacksLookup);
        enemyAttacks |= getAttacksFromAll(p, PieceIndex.QUEENS.id, Piece::queenAttacksLookup);
    }

    private void updateSlidingRay(long attacker, byte sq, int[] dirs) {
        for (int dir: dirs){
            long attacks = Piece.getRayAttacks(attacker, dir, sq);
            if ((attacks & attacker) != 0) {
                checkRay = attacks;
                break;
            }
        }
    }

    private void calculateCheckRay() {
        if (Long.bitCount(attackers) == 1) {
            if (checkersByPiece[PieceIndex.PAWNS.id] != 0) {checkRay = checkersByPiece[PieceIndex.PAWNS.id]; return;}
            if (checkersByPiece[PieceIndex.KNIGHTS.id] != 0) {checkRay = checkersByPiece[PieceIndex.KNIGHTS.id]; return;}
            if (checkersByPiece[PieceIndex.ROOKS.id] != 0) {
                updateSlidingRay(checkersByPiece[PieceIndex.ROOKS.id], kingSquare, rookDirs); return;}
            if (checkersByPiece[PieceIndex.BISHOPS.id] != 0) {
                updateSlidingRay(checkersByPiece[PieceIndex.BISHOPS.id], kingSquare, bishopDirs); return; }
            if (checkersByPiece[PieceIndex.QUEENS.id] != 0) updateSlidingRay(checkersByPiece[PieceIndex.QUEENS.id], kingSquare, queenDirs);
        }
    }

    private void calculatePinnedFromDir(Position p, long potentialAttackers, int[] dirs) {
        for (int dir: dirs) {
            long ray = Piece.rayAttacks[dir][kingSquare];
            long blockers = ray & p.occupied;
            byte firstBlocker = BitboardUtilities.bitScan(blockers, Piece.isNegative(dir));
            long firstBlockerBB = 1L << firstBlocker;
            blockers &= ~firstBlockerBB;
            if (((firstBlockerBB & p.piecesBB[color]) != 0) && (blockers != 0)) {
                long secondBlockerBB = 1L << BitboardUtilities.bitScan(blockers, Piece.isNegative(dir));
                if ((secondBlockerBB & potentialAttackers) != 0) {
                    pinned |= firstBlockerBB;
                    pinMasks[firstBlocker] = ray;
                }
            }
        }
    }

    private void calculateAllPinnedPieces(Position p) {
        long rooksAndQueens = p.piecesBB[opColor] & (p.piecesBB[PieceIndex.ROOKS.id] | p.piecesBB[PieceIndex.QUEENS.id]);
        calculatePinnedFromDir(p, rooksAndQueens, rookDirs);
        long bishopAndQueens = p.piecesBB[opColor] & (p.piecesBB[PieceIndex.BISHOPS.id] | p.piecesBB[PieceIndex.QUEENS.id]);
        calculatePinnedFromDir(p, bishopAndQueens, bishopDirs);
    }

    public void update(Position p) {
        reset(p);
        calculateAllAttacks(p);
        calculateCheckRay();
        calculateAllPinnedPieces(p);
    }
}
