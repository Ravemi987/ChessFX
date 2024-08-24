package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.model.Move;
import fr.chessproject.chessfx.model.MoveList;
import fr.chessproject.chessfx.model.Position;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelPerftExecutor {
    private final Position currentPosition;

    private final int nThreads = 4;

    public ParallelPerftExecutor(String fen) {
        currentPosition = new Position();
        currentPosition.loadFEN(fen);
    }

    public long perft(int depth) {
        MoveList moveList = currentPosition.generatePseudoLegalMoves();
        int count = moveList.getMvCount();
        long nodes = 0;

        try (ExecutorService executor = Executors.newFixedThreadPool(nThreads)) {
            long[] results = new long[count];

            for (int i = 0; i < count; i++) {

                final int index = i;

                executor.submit(() -> {
                    Position positionCopy = Position.copy(currentPosition);
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

    public long perftRec(Position pos, Move mv, int depth) {

        if (depth < 2) return 1;

        pos.makeMove(mv);

        var moveList = pos.generatePseudoLegalMoves();

        int count = moveList.getMvCount();
        long nodes = 0;


        for (int i = 0; i < count; i++) {
            nodes += perftRec(pos, moveList.getMove(i), depth - 1);
        }

        pos.unmakeMove(mv);

        return nodes;
    }

    public void runPerft(int maxDepth) {
        System.out.printf("Number of processors currently available : %d \n\n",Runtime.getRuntime().availableProcessors());

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
        ParallelPerftExecutor pft = new ParallelPerftExecutor("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        pft.runPerft(7);
    }
}