module fr.chessproject.chessfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires jdk.compiler;
    requires java.desktop;
    requires javafx.media;
    requires javafx.graphics;

    exports fr.chessproject.chessfx.controller;
    opens fr.chessproject.chessfx.controller to javafx.fxml;
    exports fr.chessproject.chessfx.helpers;
    opens fr.chessproject.chessfx.helpers to javafx.fxml;
    exports fr.chessproject.chessfx.main;
    opens fr.chessproject.chessfx.main to javafx.fxml;
//    exports fr.chessproject.chessfx.model;
//    opens fr.chessproject.chessfx.model to javafx.fxml;
    exports fr.chessproject.chessfx.view;
    opens fr.chessproject.chessfx.view to javafx.fxml;
    exports fr.chessproject.chessfx.model.UCI;
    opens fr.chessproject.chessfx.model.UCI to javafx.fxml;
    exports fr.chessproject.chessfx.model.Engine;
    opens fr.chessproject.chessfx.model.Engine to javafx.fxml;
    exports fr.chessproject.chessfx.model.Board;
    opens fr.chessproject.chessfx.model.Board to javafx.fxml;
    exports fr.chessproject.chessfx.model.Game;
    opens fr.chessproject.chessfx.model.Game to javafx.fxml;
    exports fr.chessproject.chessfx.view.Manager;
    opens fr.chessproject.chessfx.view.Manager to javafx.fxml;
    exports fr.chessproject.chessfx.view.Components;
    opens fr.chessproject.chessfx.view.Components to javafx.fxml;
}