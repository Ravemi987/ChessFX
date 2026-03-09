package fr.chessproject.chessfx.model.uci;

import java.util.function.BiConsumer;

public interface UciCommand {
    String name();
    boolean matches(String input);
    void execute(String input, BiConsumer<String, String[]> callback);
}
