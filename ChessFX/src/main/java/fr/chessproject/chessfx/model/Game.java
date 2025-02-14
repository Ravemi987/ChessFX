package fr.chessproject.chessfx.model;

public class Game {

    private final Position currentPos;

    private MoveList validMoves;
    private Move lastMove;

    public Game() {
        currentPos = new Position();
        currentPos.loadFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        //currentPos.loadFEN("2n1p1k1/3P4/8/8/8/8/5p2/2K1Q1R1 w - - 0 1");
        //currentPos.loadFEN("8/6k1/8/3q4/8/3N4/7r/R2K4 w - - 0 1");
        validMoves = currentPos.generateLegalMoves();
        lastMove = null;
    }

    public Position getPosition() {
        return currentPos;
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
        System.out.println(mv);
    }

    public Move getLastMove() {
        return lastMove;
    }

    public MoveList getValidMoves() {
        return validMoves;
    }
}
