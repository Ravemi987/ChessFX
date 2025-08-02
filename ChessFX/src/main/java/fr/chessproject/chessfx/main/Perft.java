package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.Move;
import fr.chessproject.chessfx.model.MoveList;
import fr.chessproject.chessfx.model.Position;
import fr.chessproject.chessfx.model.Zobrist;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Perft {
    private final Position currentPosition;
    private String fen;
    private ChessController controller = new ChessController();

    public Perft(String fen) {
        currentPosition = new Position();
        currentPosition.loadFEN(fen);
        this.fen = fen;
    }

    public long parallelPerft(int depth, int nbThreads) {
        MoveList moveList = currentPosition.generateLegalMoves();
        int count = moveList.getMvCount();
        long nodes = 0;

        try (ExecutorService executor = Executors.newFixedThreadPool(nbThreads)) {
            long[] results = new long[count];

            for (int i = 0; i < count; i++) {

                final int index = i;

                executor.submit(() -> {
                    Position positionCopy = new Position();
                    positionCopy.loadFEN(fen);
                    results[index] = perftRec(positionCopy, moveList.getMove(index), depth);
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {}

            for (long result : results) {
                nodes += result;
            }
        }

        return nodes;
    }

    public long perft(int depth) {
        MoveList moveList = currentPosition.generateLegalMoves();

        int count = moveList.getMvCount();
        long nodes = 0;

        for (int i = 0; i < count; i++) {
            nodes += perftRec(currentPosition, moveList.getMove(i), depth);
        }
        return nodes;
    }

    public static long perftRec(Position pos, Move mv, int depth) {

        if (depth < 2) return 1;

        long hash1 = pos.hash;
        long recomputed1 = Zobrist.computeHash(pos);
        assert recomputed1 == hash1;

        pos.makeMove(mv);

        var moveList = pos.generateLegalMoves();

        int count = moveList.getMvCount();
        long nodes = 0;

        for (int i = 0; i < count; i++) {
            nodes += perftRec(pos, moveList.getMove(i), depth - 1);
        }

        pos.unmakeMove(mv);

        long hash2 = pos.hash;
        assert hash1 == hash2;
        long recomputed2 = Zobrist.computeHash(pos);
        assert recomputed2 == hash2;

        return nodes;
    }

    public void runPerft(int maxDepth, int nbThreads) {
        int currentProcessorsNumber = Runtime.getRuntime().availableProcessors();
        if (nbThreads > currentProcessorsNumber) {
            System.out.println("Invalid argument: currently " + currentProcessorsNumber + " processors available.");
            return;
        }

        long totalNodes = 0;
        long nodes;

        for (int depth = 1; depth <= maxDepth; depth++) {

            System.out.printf("%-10s %-15s %-15s\n","depth", "nodes", "totalnodes");

            long startTime = System.nanoTime();
            nodes = nbThreads <= 1 ? perft(depth) : parallelPerft(depth, nbThreads);
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
        pft.runPerft(7, 12);
    }
}
