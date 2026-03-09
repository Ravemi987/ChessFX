package fr.chessproject.chessfx.model.uci;

public interface CommandListenerObserver {
    void onCommandReceived(String command, String... args);
}
