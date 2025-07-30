package fr.chessproject.chessfx.model.UCI;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayCommand implements UciCommand {
    private static final Pattern DISPLAY_PATTERN = Pattern.compile("d");

    @Override
    public String name() {
        return "display";
    }

    @Override
    public boolean matches(String input) {
        return DISPLAY_PATTERN.matcher(input).matches();
    }

    @Override
    public void execute(String input, BiConsumer<String, String[]> callback) {
        callback.accept("display", new String[]{""});
    }
}
