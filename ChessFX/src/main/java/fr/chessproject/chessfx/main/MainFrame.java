package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.view.GameInitializer;
import fr.chessproject.chessfx.model.uci.CommandListener;
import fr.chessproject.chessfx.view.MainFrameController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
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

        GridPane root = mainFrameLoader.load();
        MainFrameController frameController = mainFrameLoader.getController();

        frameController.setChessController(controller);
        controller.setOnBoardUpdated(() -> {
            Platform.runLater(frameController::updateBoard);
        });

        if (commandListener != null) {
            commandListener.addObserver(controller);
            commandListener.addObserver(frameController);
        }

        GameInitializer.preloadAll(frameController);
        frameController.init();

        Scene scene = new Scene(root);

        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("\nShutting down application...");
            stopCLI();
        });
    }
}
