package fr.chessproject.chessfx.main;

import com.sun.tools.javac.Main;
import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.CommandListener;
import fr.chessproject.chessfx.model.Game;
import fr.chessproject.chessfx.view.MainFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFrame extends Application {

    private static boolean debugMode = false;
    private static CommandListener commandListener;

    public static void setDebugMode(boolean debugMode) {
        MainFrame.debugMode = debugMode;
    }

    public static void setCommandListener(CommandListener cmdListener) {
        MainFrame.commandListener = cmdListener;
    }

    public void stopCLI() {
        if (commandListener != null) {
            commandListener.stopListening();
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        ChessController controller = new ChessController();

        FXMLLoader mainFrameLoader = new FXMLLoader(getClass().getResource("/fr/chessproject/chessfx/main/MainFrame.fxml"));
        BorderPane root = mainFrameLoader.load();
        MainFrameController frameController = mainFrameLoader.getController();

        //System.out.println("MainFrameController loaded");

        controller.setFrameController(frameController);
        frameController.setMainController(controller);
        commandListener.addObserver(controller);
        commandListener.addObserver(frameController);

        //System.out.println("ChessController set in MainFrameController");
        frameController.init();

        if (debugMode) {
            controller.enableDebugMode();
        }

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        Scene scene = new Scene(root, screenWidth, screenHeight);

        primaryStage.centerOnScreen();
        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("\nShutting down application...");
            stopCLI();
        });
    }
}
