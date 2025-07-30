package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.CommandListenerObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;


public class MainFrameController implements CommandListenerObserver {

    @FXML
    public BorderPane mainFrame;

    @FXML
    public Pane boardPane;

    private ChessController controller;

    private GamePanelController gamePanelController;

    public MainFrameController() {
        //System.out.println("MainFrameController created");
    }

    @Override
    public void onCommandReceived(String command, String... args) {
        if (command.equals("quit")) {
            System.out.println("Shutting down application...");
            Platform.exit();
        }
    }

    @FXML
    public void initialize() {
        //System.out.println("MainFrameController initialized");
        gamePanelController = (GamePanelController) boardPane.getProperties().get("controller");
    }

    public void init() {
        gamePanelController.init();
    }

    public void setMainController(ChessController chessController) {
        this.controller = chessController;
        gamePanelController.setMainController(controller);
    }

    public void updateBoard() {
        gamePanelController.refreshBoard();
    }

    public void enableDebugMode() {
        // Implementation for enabling debug mode
    }
}
