package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainFrameController {

    @FXML
    public BorderPane mainFrame;

    @FXML
    public Pane boardPane;

    private ChessController controller;

    private GamePanelController gamePanelController;

    public MainFrameController() {
        System.out.println("MainFrameController created");
    }

    @FXML
    public void initialize() {
        System.out.println("MainFrameController initialized");
        gamePanelController = (GamePanelController) boardPane.getProperties().get("controller");
    }

    public void init() {
        gamePanelController.init();
    }

    public void setMainController(ChessController chessController) {
        this.controller = chessController;
        gamePanelController.setMainController(controller);
    }

    public void setVisible(boolean visible) {
        // Implementation for setting visibility
    }

    public void resetGUI() {
        // Implementation for resetting the UI
    }

    public void enableDebugMode() {
        // Implementation for enabling debug mode
    }
}
