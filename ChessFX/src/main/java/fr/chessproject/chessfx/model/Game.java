package fr.chessproject.chessfx.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    private Position currentPos;
    private MoveList validMoves;
    private Move lastMove;
    private boolean gameInProgress = false;
    private final List<MoveListener> moveListeners = new ArrayList<>();
    private final List<Move> moveHistory = new ArrayList<>();
    private final Map<Long, Integer> hashHistory = new HashMap<>();
    private static final String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public Game() {
        reset();
    }

    public void setFen(String fen) {
        currentPos = new Position();
        currentPos.loadFEN(fen);
        initMoves();
    }

    public void reset() {
        currentPos = new Position();
        currentPos.loadFEN(startFen);
        initMoves();
    }

    public void initMoves() {
        validMoves = currentPos.generateLegalMoves();
        lastMove = null;
        moveHistory.clear();
        hashHistory.clear();
        hashHistory.put(currentPos.getHash(), 1);
        gameInProgress = false;
    }

    public void start() {
        this.gameInProgress = true;
    }

    public String getFen() {
        return currentPos.getFEN();
    }

    public Position getPosition() {
        return currentPos;
    }

    public boolean isInProgress() {
        return gameInProgress;
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

    public void updateHashHistory() {
        long currentHash = currentPos.getHash();
        if (hashHistory.containsKey(currentHash)) {
            hashHistory.put(currentHash, hashHistory.get(currentHash) + 1);
        } else {
            hashHistory.put(currentHash, 1);
        }
    }

    public void playMoveCLI(Move mv) {
        if (gameInProgress && isOver()) return;

        Position previous = currentPos.copy();
        currentPos.makeMove(mv);
        moveHistory.add(mv);
        updateHashHistory();
        validMoves = currentPos.generateLegalMoves();
        lastMove = mv;

        for (MoveListener listener: moveListeners) {
            if (gameInProgress && isOver()) {
                listener.onGameOver();
            } else {
                listener.onMovePlayed(mv, previous, currentPos);
            }
        }
    }

    public void playMoveGUI(Move mv) {
        if (gameInProgress && isOver()) return;

        currentPos.makeMove(mv);
        moveHistory.add(mv);
        updateHashHistory();
        validMoves = currentPos.generateLegalMoves();
        lastMove = mv;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public MoveList getValidMoves() {
        return validMoves;
    }

    public boolean isStalemate() {
        return validMoves.getMvCount() == 0 & !currentPos.isInCheck();
    }

    public boolean isThreefoldRepetition() {
       return hashHistory.containsValue(3);
    }

    public boolean isFiftyMoveRule() {
        return currentPos.getHalfMoveClock() >= 100;
    }

    public boolean isInsufficientMaterial() {
        return currentPos.isInsufficientMaterial();
    }

    public boolean isCheckmate() {
        return validMoves.getMvCount() == 0 && currentPos.isInCheck();
    }

    public boolean isDraw() {
        return isStalemate() || isThreefoldRepetition() || isFiftyMoveRule() || isInsufficientMaterial();
    }

    public boolean canClaimDraw() {
        return isThreefoldRepetition() || isFiftyMoveRule();
    }

    public boolean isAutoDraw() {
        return isStalemate() || isInsufficientMaterial();
    }

    public boolean isOver() {
        return isCheckmate() || isDraw();
    }

    // Helpers

    public long getAttackInfoCheckMask() {
        return currentPos.getAttackInfo().checkRay;
    }

    public long getAttackInfoAttackMask() {
        return currentPos.getAttackInfo().enemyAttacks;
    }

    public long getAttackInfoPinnedPieces() {
        return currentPos.getAttackInfo().pinned;
    }

    public long getEpBitboard() {
        return 1L << currentPos.getEpSquare();
    }
}
