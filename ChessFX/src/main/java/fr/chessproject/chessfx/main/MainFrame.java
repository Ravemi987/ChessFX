package fr.chessproject.chessfx.main;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.Game;
import fr.chessproject.chessfx.view.GamePanelController;
import fr.chessproject.chessfx.view.MainFrameController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFrame extends Application {

    private static boolean debugMode = false;

    public static void setDebugMode(boolean debugMode) {
        MainFrame.debugMode = debugMode;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        ChessController controller = new ChessController(new Game());

        FXMLLoader mainFrameLoader = new FXMLLoader(getClass().getResource("/fr/chessproject/chessfx/main/MainFrame.fxml"));
        BorderPane root = mainFrameLoader.load();
        MainFrameController frameController = mainFrameLoader.getController();

        System.out.println("MainFrameController loaded");

        controller.setFrameController(frameController);
        frameController.setMainController(controller);

        System.out.println("ChessController set in MainFrameController");
        frameController.init();


        controller.initDialog();

        if (debugMode) {
            controller.enableDebugMode();
        }

        Scene scene = new Scene(root, 1920, 1080);
        primaryStage.centerOnScreen();
        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
