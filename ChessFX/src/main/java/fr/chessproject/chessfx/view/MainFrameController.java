package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.controller.ChessController;
import fr.chessproject.chessfx.model.CommandListenerObserver;
import fr.chessproject.chessfx.model.PieceIndex;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import fr.chessproject.chessfx.helpers.Constants;

import java.util.List;


public class MainFrameController implements CommandListenerObserver {

    @FXML
    public BorderPane mainFrame;
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
    public VBox centerContainer;
    @FXML
    public VBox rightContainer;

    @FXML
    public HBox topbar;
    @FXML
    public HBox bottombar;
    @FXML
    public HBox toolbar;

    @FXML
    public StackPane clock_pane_1;
    @FXML
    public StackPane clock_pane_2;

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

    private void initCenterContainer() {
        centerContainer.setMaxWidth(Constants.BOARD_SIZE);
        centerContainer.setMinWidth(Constants.BOARD_SIZE);
        centerContainer.setPrefWidth(Constants.BOARD_SIZE);

        topbar.setPrefHeight(Constants.SCREEN_SIZE - ((double) Constants.REAL_BOARD_SIZE / 2) - 10);
        bottombar.setPrefHeight(Constants.SCREEN_SIZE - ((double) Constants.REAL_BOARD_SIZE / 2) - 10);

        clock_pane_1.setPrefWidth(1.7 * ((double) Constants.REAL_BOARD_SIZE / 8));
        clock_pane_2.setPrefWidth(1.7 * ((double) Constants.REAL_BOARD_SIZE / 8));
    }

    private void initRightContainer() {
        rightContainer.setMaxHeight(Constants.BOARD_SIZE);
        rightContainer.setMinHeight(Constants.BOARD_SIZE);
        rightContainer.setPrefHeight(Constants.BOARD_SIZE);
    }

    @FXML
    public void initialize() {
        //System.out.println("MainFrameController initialized");
        gamePanelController = (GamePanelController) boardPane.getProperties().get("controller");
        initCenterContainer();
        initRightContainer();

        toolbar.setSpacing(10);

        for (Button btn : List.of(
                newGameButton, flipBoardButton, firstMoveButton,
                prevMoveButton, nextMoveButton, lastMoveButton)) {
            ButtonUtilities.loadButtonIcon(btn);
        }

        toolbar.setMaxWidth(Region.USE_PREF_SIZE);
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
        controller.setGameClocks(clock_pane_1, clock_pane_2);
        gamePanelController.setChessController(controller);
    }

    public void updateBoard() {
        gamePanelController.refreshBoard();
    }

    public void enableDebugMode() {
        // Implementation for enabling debug mode
    }
}
