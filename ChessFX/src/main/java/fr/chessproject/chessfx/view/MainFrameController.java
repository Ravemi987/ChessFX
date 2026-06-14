package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.uci.CommandListenerObserver;
import fr.chessproject.chessfx.view.components.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import fr.chessproject.chessfx.helpers.Constants;

import java.util.List;

public class MainFrameController implements CommandListenerObserver {

    @FXML
    public GridPane mainFrame;
    @FXML
    public Pane boardPane;
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
    public VBox leftContainer;
    @FXML
    public VBox centerContainer;
    @FXML
    public VBox rightContainer;
    @FXML
    public VBox gameControlArea;
    @FXML
    public HBox toolbar;

    @FXML
    public StackPane clockPane1;
    @FXML
    public StackPane clockPane2;

    private ChessController controller;
    private GamePanelController gamePanelController;
    private ClockView whiteClockView;
    private ClockView blackClockView;
    private final Config config;

    public MainFrameController() {
        this.config = new Config();
    }

    @Override
    public void onCommandReceived(String command, String... args) {
        if (command.equals("quit")) {
            System.out.println("Shutting down application...");
            Platform.exit();
        }
    }

    private void initSideContainers() {
        double targetHeight = Constants.BOARD_SIZE;

        leftContainer.setPrefHeight(targetHeight);
        leftContainer.setMinHeight(targetHeight);
        leftContainer.setMaxHeight(targetHeight);
        leftContainer.setMaxWidth(Double.MAX_VALUE);

        rightContainer.setPrefHeight(targetHeight);
        rightContainer.setMinHeight(targetHeight);
        rightContainer.setMaxHeight(targetHeight);
        rightContainer.setMaxWidth(Double.MAX_VALUE);
    }

    @FXML
    public void initialize() {
        gamePanelController = (GamePanelController) boardPane.getProperties().get("controller");

        initSideContainers();

        toolbar.setSpacing(10);

        for (Button btn : List.of(
                newGameButton, flipBoardButton, firstMoveButton,
                prevMoveButton, nextMoveButton, lastMoveButton)) {
            ButtonUtilities.loadButtonIcon(btn);
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        toolbar.setMaxWidth(Double.MAX_VALUE);

        whiteClockView = new ClockView(clockPane1);
        blackClockView = new ClockView(clockPane2);

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

    public void setChessController(ChessController controller) {
        this.controller = controller;
        gamePanelController.setChessController(controller, config.getTheme());
    }

    public void refreshClocks() {
        whiteClockView.update(controller.getGame().getWhiteClock());
        blackClockView.update(controller.getGame().getBlackClock());
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

    public void updateBoard() {
        gamePanelController.refreshBoard();
    }

    public void preloadSprites() {
        gamePanelController.preloadSprites();
    }

    public void enableDebugMode() {}
}
