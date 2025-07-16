package fr.chessproject.chessfx.model.UCI;

import fr.chessproject.chessfx.model.CommandListenerObserver;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoCommand implements UciCommand {
    private static final Pattern PERFT_PATTERN = Pattern.compile("^go perft (\\d+)$");
    private static final Pattern DIVIDE_PATTERN = Pattern.compile("^go divide (\\d+)$");


    @Override
    public String name() {
        return "go";
    }

    @Override
    public boolean matches(String input) {
        return PERFT_PATTERN.matcher(input).matches() || DIVIDE_PATTERN.matcher(input).matches();
    }

    @Override
    public void execute(String input, BiConsumer<String, String[]> callback) {
        Matcher m1 = PERFT_PATTERN.matcher(input);
        Matcher m2 = DIVIDE_PATTERN.matcher(input);
        if (m1.matches()) {
            String depth = m1.group(1);
            callback.accept("go", new String[]{"perft", depth});
        } else if (m2.matches()) {
            String depth = m2.group(1);
            callback.accept("go", new String[]{"divide", depth});
        }
    }
}
