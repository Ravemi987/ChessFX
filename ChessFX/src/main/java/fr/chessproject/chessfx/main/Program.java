package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.Game;
import javafx.application.Platform;

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

        boolean finalDebugMode = debugMode;
        Platform.runLater(() -> {
            ChessController dialog = new ChessController(new Game());
            dialog.initDialog();
            if (finalDebugMode) dialog.enableDebugMode();
        });
    }
}
