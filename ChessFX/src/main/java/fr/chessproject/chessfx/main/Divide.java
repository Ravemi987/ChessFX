package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.model.Move;
import fr.chessproject.chessfx.model.MoveList;
import fr.chessproject.chessfx.model.Position;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static fr.chessproject.chessfx.main.Perft.perftRec;

public class Divide {
    private final Position currentPosition;
    private final int MAXDEPTH = 7;

    public Divide(String fen) {
        currentPosition = new Position();
        currentPosition.loadFEN(fen);
    }

    public long parallelDivide(int depth, int nbThreads) {
        MoveList moveList = currentPosition.generateLegalMoves();
        int count = moveList.getMvCount();
        long nodes = 0;

        try (ExecutorService executor = Executors.newFixedThreadPool(nbThreads)) {
            long[] results = new long[count];

            for (int i = 0; i < count; i++) {

                final int index = i;

                executor.submit(() -> {
                    Position positionCopy = Position.copy(currentPosition);
                    results[index] = divide(positionCopy, moveList.getMove(index), depth);
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

    public long divide(int depth) {
        MoveList moveList = currentPosition.generateLegalMoves();

        int count = moveList.getMvCount();
        long nodes = 0;

        for (int i = 0; i < count; i++) {
            nodes += divide(currentPosition, moveList.getMove(i), depth);
        }

        return nodes;
    }

    public long divide(Position pos, Move mv, int depth) {
        long res = perftRec(pos, mv, depth);
        System.out.printf("%-10s %10d%n", mv.toString(), res);

        return res;
    }

    public void runDivide(int depth, int nbThreads) {
        int currentProcessorsNumber = Runtime.getRuntime().availableProcessors();
        if (depth > MAXDEPTH || nbThreads > currentProcessorsNumber) {
            System.out.println("Invalid argument: currently " + currentProcessorsNumber + " processors available.");
            return;
        }

        long nodes;

        long startTime = System.nanoTime();
        nodes = nbThreads <= 1 ? divide(depth) : parallelDivide(depth, nbThreads);
        long endTime = System.nanoTime();

        long elapsedTime = endTime - startTime;
        double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0;
        double elapsedTimeInMilliseconds = elapsedTime / 1_000_000.0;
        double mnps = (nodes / elapsedTimeInSeconds) / 1_000_000.0;

        System.out.print("\n");
        System.out.printf("Divide %d: %d nodes / %.0fms / %.2f MNodes/s\n", depth, nodes, elapsedTimeInMilliseconds, mnps);
        System.out.println("-----------------------------------------------------");
        }

    public static void main(String[] args) {
        Divide divide = new Divide("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        divide.runDivide(6, 12);
    }
}
