package fr.chessproject.chessfx.model;

import java.util.ArrayList;

public class MoveList {
    private final ArrayList<Move> moves;
    private static final int initialCapacity = 100;
    private int mvCount;

    public MoveList() {
        moves = new ArrayList<>(initialCapacity);
        mvCount = 0;
    }

    public void addMove(Move move) {
        moves.add(move);
        mvCount++;
    }

    public Move getMove(int index) {
        return moves.get(index);
    }

    public int getMvCount() {
        return mvCount;
    }
}
