package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.view.Clock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    private Position currentPos;
    private MoveList validMoves;
    private Move lastMove;
    private final List<MoveListener> moveListeners = new ArrayList<>();
    private final List<Move> moveHistory = new ArrayList<>();
    private final Map<Long, Integer> hashHistory = new HashMap<>();
    private static final String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private GameMode gameMode = GameMode.FREE_PLAY;
    private GameState gameState = GameState.NOT_STARTED;

    private Clock clock1;
    private Clock clock2;
    private double initialTimePerPlayer = 20;
    private double incrementPerMove = 1;

    public Game() {
        reset();
    }

    public void setFen(String fen) {
        currentPos = new Position();
        currentPos.loadFEN(fen);
        initMoves();
    }

    public void setClocks(Clock clk1, Clock clk2) {
        this.clock1 = clk1;
        this.clock2 = clk2;
        clock1.init(initialTimePerPlayer);
        clock2.init(initialTimePerPlayer);
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
    }

    public void startCompetitiveGame() {
        this.gameMode = GameMode.COMPETITIVE;
        this.gameState = GameState.IN_PROGRESS;

        for (MoveListener listener: moveListeners) {
            listener.onGameStarted();
        }

        clock1.resume();
    }

    public String getFen() {
        return currentPos.getFEN();
    }

    public Position getPosition() {
        return currentPos;
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean cannotPlay() {
        if (gameMode == GameMode.FREE_PLAY) {
            return false;
        }
        return gameState != GameState.IN_PROGRESS && gameState != GameState.NOT_STARTED;
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

    private void stopClocks() {
        clock1.stop();
        clock2.stop();
    }

    private void switchClock(Clock old, Clock now) {
        old.pause();
        old.setTime(old.getTime() + incrementPerMove);
        now.resume();
    }

    public void updateClocks() {
        if (clock1.isTicking()) {
            switchClock(clock1, clock2);
        } else {
            switchClock(clock2, clock1);
        }
    }

    public void changeTurn(Move mv) {
        currentPos.makeMove(mv);
        moveHistory.add(mv);
        updateHashHistory();
        validMoves = currentPos.generateLegalMoves();
        lastMove = mv;
        updateGameState();

        if (gameMode == GameMode.COMPETITIVE) {
            updateClocks();
        }
    }

    public void playMoveCLI(Move mv) {
        if (cannotPlay()) stopClocks();
        changeTurn(mv);

        for (MoveListener listener: moveListeners) {
            if (cannotPlay()) {
                listener.onGameOver();
            } else {
                listener.onMovePlayed(mv);
            }
        }
    }

    public void playMoveGUI(Move mv) {
        if (cannotPlay()) stopClocks();
        changeTurn(mv);
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

    public boolean canClaimDraw() {
        return isThreefoldRepetition() || isFiftyMoveRule();
    }

    public void updateGameState() {
        if (isCheckmate()) {
            gameState = currentPos.getTurn() == PieceIndex.WHITE_PIECES.id ? GameState.BLACK_WON : GameState.WHITE_WON;
        } else if (isStalemate()) {
            gameState = GameState.STALEMATE;
        } else if (isThreefoldRepetition()) {
            gameState = GameState.THREEFOLD_REPETITION;
        } else if (isFiftyMoveRule()) {
            gameState = GameState.FIFTY_MOVE_RULE;
        } else if (isInsufficientMaterial()) {
            gameState = GameState.INSUFFICIENT_MATERIAL;
        } else {
            gameState = GameState.IN_PROGRESS;
        }
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
