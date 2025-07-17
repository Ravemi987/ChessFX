package fr.chessproject.chessfx.model.UCI;

import fr.chessproject.chessfx.model.CommandListenerObserver;

import java.util.function.BiConsumer;

public interface UciCommand {
    String name();
    boolean matches(String input);
    void execute(String input, BiConsumer<String, String[]> callback);
}
