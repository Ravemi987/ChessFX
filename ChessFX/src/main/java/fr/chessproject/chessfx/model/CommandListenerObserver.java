package fr.chessproject.chessfx.model;

public interface CommandListenerObserver {
    void onCommandReceived(String command, String... args);
}
