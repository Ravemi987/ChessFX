package fr.chessproject.chessfx.model.Board;

import java.util.Arrays;

public class MoveList {
    private final Move[] moves;
    private static final int maxCapacity = 218;
    private int mvCount;

    public MoveList() {
        moves = new Move[maxCapacity];
        mvCount = 0;
    }

    public void addMove(Move move) {
        moves[mvCount++] = move;
    }

    public Move getMove(int index) {
        return moves[index];
    }

    public int getMvCount() {
        return mvCount;
    }

    @Override
    public String toString() {
        return Arrays.toString(moves);
    }
}
