package fr.chessproject.chessfx.model.game;

import fr.chessproject.chessfx.model.board.Move;
import fr.chessproject.chessfx.model.board.MoveList;
import fr.chessproject.chessfx.model.board.PieceIndex;
import fr.chessproject.chessfx.model.board.Position;

import java.util.*;

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

    private double initialTimePerPlayer = 10;
    private double incrementPerMove = 2;

    private ClockModel whiteClock = new ClockModel(initialTimePerPlayer);
    private ClockModel blackClock = new ClockModel(initialTimePerPlayer);

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
    }

    public void startCompetitiveGame() {
        this.gameMode = GameMode.COMPETITIVE;
        this.gameState = GameState.IN_PROGRESS;

        whiteClock.reset(initialTimePerPlayer);
        blackClock.reset(initialTimePerPlayer);

        for (MoveListener listener: moveListeners) {
            listener.onGameStarted();
        }

        whiteClock.start();
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

    public ClockModel getWhiteClock() {
        return whiteClock;
    }

    public ClockModel getBlackClock() {
        return blackClock;
    }

    public void onTimeout(PieceIndex loser) {
        if (loser == PieceIndex.WHITE_PIECES) {
            gameState = GameState.WHITE_TIMEOUT;
        } else {
            gameState = GameState.BLACK_TIMEOUT;
        }

        for (MoveListener listener: moveListeners) {
            listener.onGameOver();
        }
    }

    public boolean hasTimeout() {
        return gameState == GameState.WHITE_TIMEOUT
                || gameState == GameState.BLACK_TIMEOUT;
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

    private void switchClock(ClockModel old, ClockModel now) {
        old.stop();
        old.addTime(incrementPerMove);
        now.start();
    }

    public void updateClocks() {
        if (whiteClock.isTicking()) {
            switchClock(whiteClock, blackClock);
        } else {
            switchClock(blackClock, whiteClock);
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

    public void playMove(Move mv) {
        if (cannotPlay()) return;
        changeTurn(mv);

        for (MoveListener listener: moveListeners) {
            if (cannotPlay()) {
                listener.onGameOver();
            } else {
                listener.onMovePlayed(mv);
            }
        }
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
