package fr.chessproject.chessfx.model.UCI;

import java.util.function.BiConsumer;

public interface UciCommand {
    String name();
    boolean matches(String input);
    void execute(String input, BiConsumer<String, String[]> callback);
}
