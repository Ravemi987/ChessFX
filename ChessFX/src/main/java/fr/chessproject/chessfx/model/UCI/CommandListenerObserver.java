package fr.chessproject.chessfx.model.UCI;

public interface CommandListenerObserver {
    void onCommandReceived(String command, String... args);
}
