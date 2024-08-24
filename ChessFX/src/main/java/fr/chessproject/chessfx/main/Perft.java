package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.model.Move;
import fr.chessproject.chessfx.model.MoveList;
import fr.chessproject.chessfx.model.Position;

public class Perft {

    private final Position currentPosition;

    public Perft(String fen) {
        currentPosition = new Position();
        currentPosition.loadFEN(fen);
    }

    public long perft(int depth) {
        MoveList moveList = currentPosition.generatePseudoLegalMoves();

        int count = moveList.getMvCount();
        long nodes = 0;

        for (int i = 0; i < count; i++) {
            nodes += perft(moveList.getMove(i), depth);
        }
        return nodes;
    }

    public long perft(Move mv, int depth) {

        if (depth < 2)
            return 1;

        currentPosition.makeMove(mv);

        var moveList = currentPosition.generatePseudoLegalMoves();

        int count = moveList.getMvCount();
        long nodes = 0;

        for (int i = 0; i < count; i++) {
            nodes += perft(moveList.getMove(i), depth - 1);
        }

        currentPosition.unmakeMove(mv);

        return nodes;
    }

    public void runPerft(int maxDepth) {
        long totalNodes = 0;
        long nodes;

        for (int depth = 1; depth <= maxDepth; depth++) {

            System.out.printf("%-10s %-15s %-15s\n","depth", "nodes", "totalnodes");

            long startTime = System.nanoTime();
            nodes = perft(depth);
            long endTime = System.nanoTime();

            long elapsedTime = endTime - startTime;
            double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0;
            double elapsedTimeInMilliseconds = elapsedTime / 1_000_000.0;
            double mnps = (nodes / elapsedTimeInSeconds) / 1_000_000.0;

            totalNodes += nodes;

            System.out.printf("%-10d %-15d %-15d\n", depth, nodes, totalNodes);
            System.out.println("-----------------------------------------------------");
            System.out.printf("Perft Start %d: %d nodes / %.0fms / %.2f MNodes/s\n", depth, nodes, elapsedTimeInMilliseconds, mnps);
            System.out.print("\n");
        }
    }

    public static void main(String[] args) {
        Perft pft = new Perft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        pft.runPerft(3);
    }
}
