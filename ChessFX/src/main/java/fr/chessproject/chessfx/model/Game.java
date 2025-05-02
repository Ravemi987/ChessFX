package fr.chessproject.chessfx.model;

public class Game {

    private Position currentPos;

    private MoveList validMoves;
    private Move lastMove;

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


    public Move checkMove(Move mv) {
        for (int i = 0; i < validMoves.getMvCount(); i++) {
            if (mv.equals(validMoves.getMove(i))) {
                return validMoves.getMove(i);
            }
        }
        return null;
    }

    public void playMove(Move mv) {
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
}
