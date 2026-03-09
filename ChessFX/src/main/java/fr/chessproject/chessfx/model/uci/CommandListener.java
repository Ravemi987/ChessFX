package fr.chessproject.chessfx.model.uci;

import java.util.*;

public class CommandListener implements Runnable {
    private volatile boolean running = true;
    private final Scanner scanner;
    private final CommandDispatcher dispatcher = new CommandDispatcher();
    private final List<CommandListenerObserver> observers = new ArrayList<>();

    public CommandListener() {
        this.scanner = new Scanner(System.in);
    }

    public void addObserver(CommandListenerObserver observer) {
        observers.add(observer);
    }

    private void notifyObserver(String command, String... args) {
        for (CommandListenerObserver observer: observers) {
            observer.onCommandReceived(command, args);
        }
    }

    @Override
    public void run() {
        System.out.println("CLI started. Type 'quit' to exit.");
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                dispatcher.dispatch(input, this::notifyObserver);
            }
        }
        scanner.close();
    }

    public void stopListening() {
        System.out.println("Stopping command line...");
        running = false;
    }
}
