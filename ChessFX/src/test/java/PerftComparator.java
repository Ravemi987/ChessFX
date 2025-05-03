import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PerftComparator {

    static class MovePerft {
        String move;
        int nodes;

        public MovePerft(String move, int nodes) {
            this.move = move;
            this.nodes = nodes;
        }
    }

    public static List<MovePerft> readPerftFile(String filePath) throws IOException {
        List<MovePerft> moves = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String move = parts[0].trim();
                int nodes = Integer.parseInt(parts[1].trim());
                moves.add(new MovePerft(move, nodes));
            }
        }
        reader.close();
        return moves.stream()
                .sorted(Comparator.comparing(m -> m.move))
                .collect(Collectors.toList());
    }

    public static void comparePerft(List<MovePerft> mine, List<MovePerft> stockfish) {
        Map<String, Integer> mapMine = mine.stream().collect(Collectors.toMap(m -> m.move, m -> m.nodes));
        Map<String, Integer> mapStockfish = stockfish.stream().collect(Collectors.toMap(m -> m.move, m -> m.nodes));

        Set<String> allMoves = new TreeSet<>();
        allMoves.addAll(mapMine.keySet());
        allMoves.addAll(mapStockfish.keySet());

        // ANSI colors
        String GREEN = "\u001B[32m";
        String RED = "\u001B[31m";
        String RESET = "\u001B[0m";

        // Header
        System.out.printf("%-10s | %-10s | %-10s | %-10s%n", "Move", "Mine", "Stockfish", "Status");
        System.out.println("----------------------------------------------");

        for (String move : allMoves) {
            Integer myNodes = mapMine.get(move);
            Integer sfNodes = mapStockfish.get(move);

            String status;
            if (myNodes == null || sfNodes == null) {
                status = RED + "ERROR" + RESET;
            } else if (Objects.equals(myNodes, sfNodes)) {
                status = GREEN + "OK" + RESET;
            } else {
                status = RED + "ERROR" + RESET;
            }

            String myStr = (myNodes != null) ? String.valueOf(myNodes) : "N/A";
            String sfStr = (sfNodes != null) ? String.valueOf(sfNodes) : "N/A";
            String deltaStr = (myNodes != null && sfNodes != null) ? String.valueOf(myNodes - sfNodes) : "N/A";

            System.out.printf("%-10s | %-10s | %-10s | %-10s%n", move, myStr, sfStr, deltaStr + " " + status);
        }
    }

    public static void main(String[] args) {
        try {
            // /home/rairiau/Documents/Github/ChessFX/ChessFX/src/test/java/
            List<MovePerft> mine = readPerftFile("C:\\Users\\Admin_Remi\\Documents\\GitHub\\ChessFX\\ChessFX\\src\\test\\java\\perft_engine.txt");
            List<MovePerft> stockfish = readPerftFile("C:\\Users\\Admin_Remi\\Documents\\GitHub\\ChessFX\\ChessFX\\src\\test\\java\\perft_stockfish.txt");
            comparePerft(mine, stockfish);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
