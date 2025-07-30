package fr.chessproject.chessfx.model;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private Position currentPos;
    private MoveList validMoves;
    private Move lastMove;
    private final List<MoveListener> moveListeners = new ArrayList<>();

    public Game() {
        currentPos = new Position();
        currentPos.loadFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        initMoves();
    }

    public void setFen(String fen) {
        currentPos = new Position();
        currentPos.loadFEN(fen);
        initMoves();
    }

    public String getFen() {
        return currentPos.getFEN();
    }

    public Position getPosition() {
        return currentPos;
    }

    public void initMoves() {
        validMoves = currentPos.generateLegalMoves();
        lastMove = null;
    }

    public Move checkMoveFomString(String mvStr) {
        Move mv = Move.convertFromString(mvStr);
        return checkMove(mv);
    }

    public Move checkMove(Move mv) {
        for (int i = 0; i < validMoves.getMvCount(); i++) {
            if (mv.equals(validMoves.getMove(i))) {
                return validMoves.getMove(i);
            }
        }
        return null;
    }

    public boolean isLegal(Move mv) {
        return checkMove(mv) != null;
    }

    public void addMoveListener(MoveListener listener) {
        moveListeners.add(listener);
    }

    public void playMoveIn(Move mv) {
        Position previous = currentPos.copy();
        currentPos.makeMove(mv);
        validMoves = currentPos.generateLegalMoves();
        lastMove = mv;

        for (MoveListener listener: moveListeners) {
            listener.onMovePlayed(mv, previous, currentPos);
        }
    }

    public void playMoveOut(Move mv) {
        currentPos.makeMove(mv);
        validMoves = currentPos.generateLegalMoves();
        lastMove = mv;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public MoveList getValidMoves() {
        return validMoves;
    }

    // Helpers

    public long getAttackInfoCheckMask() {
        return currentPos.getAttackInfo().checkRay;
    }

    public long getAttackInfoAttackMask() {
        return currentPos.getAttackInfo().enemyAttacks;
    }

    public long getAttackInfoPinnedPices() {
        return currentPos.getAttackInfo().pinned;
    }

    public long getEpBitboard() {
        return 1L << currentPos.getEpSquare();
    }
}
