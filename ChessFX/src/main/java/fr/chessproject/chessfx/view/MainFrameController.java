package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.CommandListenerObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;

import java.util.List;
import java.util.Objects;


public class MainFrameController implements CommandListenerObserver {

    @FXML
    public BorderPane mainFrame;

    @FXML
    public Pane boardPane;
    @FXML
    public HBox toolbar;
    @FXML
    public Button newGameButton;
    @FXML
    public Button flipBoardButton;
    @FXML
    public Button firstMoveButton;
    @FXML
    public Button prevMoveButton;
    @FXML
    public Button nextMoveButton;
    @FXML
    public Button lastMoveButton;
    @FXML
    public VBox centerContainer;

    private ChessController controller;

    private GamePanelController gamePanelController;

    private final double SCREEN_SIZE = Screen.getPrimary().getVisualBounds().getHeight();

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
        toolbar.setSpacing(10);

        for (Button btn : List.of(
                newGameButton, flipBoardButton, firstMoveButton,
                prevMoveButton, nextMoveButton, lastMoveButton)) {
            loadButtonIcon(btn);
        }

        toolbar.setMinWidth(((SCREEN_SIZE * 0.85) / 8) * 8);
        toolbar.setMaxWidth(((SCREEN_SIZE * 0.85) / 8) * 8);
        toolbar.setMinHeight(75);
        toolbar.setMaxHeight(75);
    }

    private void loadButtonIcon(Button btn) {
        btn.getStyleClass().add(btn.getId());
        btn.setPickOnBounds(true);

        Region icon = new Region();
        icon.getStyleClass().add("icon");
        btn.setGraphic(icon);
    }

    public void init() {
        gamePanelController.init();
    }

    public void setChessController(ChessController chessController) {
        this.controller = chessController;
        gamePanelController.setChessController(controller);
    }

    public void updateBoard() {
        gamePanelController.refreshBoard();
    }

    public void enableDebugMode() {
        // Implementation for enabling debug mode
    }
}
