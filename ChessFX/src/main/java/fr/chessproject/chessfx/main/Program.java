package fr.chessproject.chessfx.main;

public class Program {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Verification des arguments du programme
        if (args.length > 1) {
            System.err.println("Usage: java Main.java DEBUG=[yes/no/true/false]");
            System.exit(1);
        }

        // Par defaut ou en cas d'erreur, le programme ne sera pas execute en mode debug
        boolean debugMode = false;

        if (args.length == 1) {
            String token = args[0].split("=")[1];
            debugMode = token.equals("yes") || token.equals("true");
        }

        launchGUI(debugMode, args);
    }

    private static void launchGUI(boolean debugMode, String[] args) {
        MainFrame.setDebugMode(debugMode);
        MainFrame.launch(MainFrame.class, args);
    }
}
