package fr.chessproject.chessfx.model.uci;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PositionCommand implements UciCommand {
    private static final Pattern STARTPOS_PATTERN = Pattern.compile("^position startpos(?: moves (.+))?$");
    private static final Pattern FEN_PATTERN = Pattern.compile("^position fen (\\S+(?:\\s\\S+){5})(?: moves (.+))?$");

    @Override
    public String name() {
        return "position";
    }

    @Override
    public boolean matches(String input) {
        return STARTPOS_PATTERN.matcher(input).matches() || FEN_PATTERN.matcher(input).matches();
    }

    @Override
    public void execute(String input, BiConsumer<String, String[]> callback) {
        Matcher m1 = STARTPOS_PATTERN.matcher(input);
        Matcher m2 = FEN_PATTERN.matcher(input);

        if (m1.matches()) {
            String moves = m1.group(1);
            callback.accept("position", new String[]{"startpos", (moves != null ? "moves" : ""), (moves != null ? moves : "")});
        } else if (m2.matches()) {
            String fen = m2.group(1);
            String moves = m2.group(2);
            callback.accept("position", new String[]{"fen", fen, (moves != null ? "moves" : ""), (moves != null ? moves : "")});
        }
    }
}
