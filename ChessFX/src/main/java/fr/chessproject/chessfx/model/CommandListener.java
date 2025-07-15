package fr.chessproject.chessfx.model;

import fr.chessproject.chessfx.main.Divide;
import fr.chessproject.chessfx.main.Perft;

import java.util.*;
import java.util.function.Consumer;

public class CommandListener implements Runnable {
    private volatile boolean running = true;
    private final Scanner scanner;
    private final Map<String, Consumer<String>> commands;
    private final List<CommandListenerObserver> observers = new ArrayList<>();

    public CommandListener() {
        this.scanner = new Scanner(System.in);
        this.commands = new HashMap<>();
        loadCommands();
    }

    public void addObserver(CommandListenerObserver observer) {
        observers.add(observer);
    }

    private void notifyObserver(String command, String args) {
        for (CommandListenerObserver observer: observers) {
            observer.onCommandReceived(command, args);
        }
    }

    public void loadCommands() {
        commands.put("perft", this::handlePerft);
        commands.put("divide", this::handleDivide);
        commands.put("setpos", this::handleSetPos);
        commands.put("getpos", (args) -> handleGetPos());
        commands.put("quit", (args) -> handleQuite());
        commands.put("help", (args) -> handleHelp());
    }

    @Override
    public void run() {
        System.out.println("CLI started. Type 'quit' to exit.");
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                processCommand(input);
            }
        }
        scanner.close();
    }

    private void processCommand(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts[0].toLowerCase(); // Command name as stored in the map
        String args = parts.length > 1 ? parts[1] : "";  // all args

        Consumer<String> action = commands.get(command); // get the adress of the function to execute
        if (action != null) {
            action.accept(args); // execute the function
        } else {
            System.out.println("Unknown command: " + command);
        }
    }

    private void handleHelp() {
        System.out.println("help");
    }

    public void stopListening() {
        System.out.println("Stopping command line...");
        running = false;
    }

    private void handleQuite() {
        stopListening();
        notifyObserver("quit", "");
    }

    private void handleGetPos() {
        notifyObserver("getpos", "");
    }

    private void handleSetPos(String s) {
        System.out.println(s);
        notifyObserver("setpos", s);
    }

    private void handleDivide(String s) {
        notifyObserver("divide", s);
    }

    private void handlePerft(String s) {
        notifyObserver("perft", s);
    }
}
