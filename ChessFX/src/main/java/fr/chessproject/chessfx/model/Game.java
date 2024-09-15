package fr.chessproject.chessfx.model;

public class Game {

    private final Position currentPos;

    private MoveList validMoves;
    private Move lastMove;

    public Game() {
        currentPos = new Position();
        //currentPos.loadFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        currentPos.loadFEN("5k2/2P5/8/8/8/8/3p4/5K2 w - - 0 1");
        validMoves = currentPos.generatePseudoLegalMoves();
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
        validMoves = currentPos.generatePseudoLegalMoves();
        lastMove = mv;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public MoveList getValidMoves() {
        return validMoves;
    }
}
