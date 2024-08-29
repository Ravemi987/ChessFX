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

    private ChessController controller;

    private GamePanelController gamePanelController;

    public MainFrameController() {
        System.out.println("MainFrameController created");
    }

    @FXML
    public void initialize() {
        System.out.println("MainFrameController initialized");
    }

    @FXML
    private void handleMouseMoved(MouseEvent mouseEvent) {
        gamePanelController.updateMousePos(new Point2D(mouseEvent.getX(), mouseEvent.getY()));
    }

    public void init() {
        loadGamePanelController();
        setupMouseEvents();
        gamePanelController.init();
    }

    public void setupMouseEvents() {
        mainFrame.setOnMouseMoved(this::handleMouseMoved);
    }

    public void setMainController(ChessController chessController) {
        this.controller = chessController;
    }

    public void loadGamePanelController() {
        try {
            FXMLLoader gamePanelLoader = new FXMLLoader(getClass().getResource("/fr/chessproject/chessfx/view/GamePanel.fxml"));
            gamePanelLoader.load();
            gamePanelController = gamePanelLoader.getController();
            gamePanelController.setMainController(controller);
            Pane gamePanel = gamePanelLoader.getRoot();
            mainFrame.setCenter(gamePanel);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
