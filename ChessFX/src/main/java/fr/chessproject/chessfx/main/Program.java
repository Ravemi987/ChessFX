package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.model.uci.CommandListener;

public class Program {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length > 1) {
            System.err.println("Usage: java Main.java DEBUG=[yes/no/true/false]");
            System.exit(1);
        }

        boolean debugMode = false;

        if (args.length == 1) {
            String token = args[0].split("=")[1];
            debugMode = token.equals("yes") || token.equals("true");
        }

        CommandListener cmdListener = launchCLI(debugMode, args);
        launchGUI(cmdListener, debugMode, args);
    }

    private static void launchGUI(CommandListener cmdListener, boolean debugMode, String[] args) {
        MainFrame.setCommandListener(cmdListener);
        MainFrame.setDebugMode(debugMode);
        MainFrame.launch(MainFrame.class, args);
    }

    private static CommandListener launchCLI(boolean debugMode, String[] args) {
        if (!debugMode) return null;

        CommandListener commandListener = new CommandListener();
        Thread commandThread = new Thread(commandListener);
        commandThread.setDaemon(true);
        commandThread.start();

        return commandListener;
    }
}
