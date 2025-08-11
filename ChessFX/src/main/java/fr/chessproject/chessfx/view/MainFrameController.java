package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.CommandListenerObserver;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Screen;

import java.util.List;


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
            ButtonUtilities.loadButtonIcon(btn);
        }

        toolbar.setMinWidth(((SCREEN_SIZE * 0.85) / 8) * 8);
        toolbar.setMaxWidth(((SCREEN_SIZE * 0.85) / 8) * 8);
        toolbar.setMinHeight(75);
        toolbar.setMaxHeight(75);
    }

    public void init() {
        newGameButton.setOnAction(_ -> handleNewGame());
        flipBoardButton.setOnAction(_ -> handleFlipBoard());
        firstMoveButton.setOnAction(_ -> goToFirstMove());
        prevMoveButton.setOnAction(_ -> goToPreviousMove());
        nextMoveButton.setOnAction(_ -> goToNextMove());
        lastMoveButton.setOnAction(_ -> goToLastMove());

        gamePanelController.init();
    }

    private void closePopups() {
        EndGamePopup.close(gamePanelController.getPopupLayer());
        PromotionPopup.close(gamePanelController.getBoardMaskPane());
    }

    private void handleNewGame() {
        controller.startNewGame();
        gamePanelController.refreshBoard();
        closePopups();
    }

    private void handleFlipBoard() {
        gamePanelController.flipBoard();
        closePopups();
    }

    private void goToFirstMove() {
        closePopups();
    }

    private void goToPreviousMove() {
        closePopups();
    }

    private void goToNextMove() {
        closePopups();
    }

    private void goToLastMove() {
        closePopups();
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
