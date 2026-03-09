package fr.chessproject.chessfx.model.uci;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class QuitCommand implements UciCommand {
    private static final Pattern QUIT_PATTERN = Pattern.compile("quit");

    @Override
    public String name() {
        return "quit";
    }

    @Override
    public boolean matches(String input) {
        return QUIT_PATTERN.matcher(input).matches();
    }

    @Override
    public void execute(String input, BiConsumer<String, String[]> callback) {
        callback.accept("quit", new String[]{""});
    }
}
