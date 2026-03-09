package fr.chessproject.chessfx.model.uci;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetOptionCommand implements UciCommand {
    private static final Pattern THREADS_PATTERN = Pattern.compile("setoption name Threads value (\\d+)");

    @Override
    public String name() {
        return "setoption";
    }

    @Override
    public boolean matches(String input) {
        return THREADS_PATTERN.matcher(input).matches();
    }

    @Override
    public void execute(String input, BiConsumer<String, String[]> callback) {
        Matcher m = THREADS_PATTERN.matcher(input);
        if (m.matches()) {
            callback.accept("setoption", new String[]{"threads", m.group(1)});
        }
    }
}
