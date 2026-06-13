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
    exports fr.chessproject.chessfx.model.uci;
    opens fr.chessproject.chessfx.model.uci to javafx.fxml;
    exports fr.chessproject.chessfx.model.engine;
    opens fr.chessproject.chessfx.model.engine to javafx.fxml;
    exports fr.chessproject.chessfx.model.board;
    opens fr.chessproject.chessfx.model.board to javafx.fxml;
    exports fr.chessproject.chessfx.model.game;
    opens fr.chessproject.chessfx.model.game to javafx.fxml;
    exports fr.chessproject.chessfx.view.animation;
    opens fr.chessproject.chessfx.view.animation to javafx.fxml;
    exports fr.chessproject.chessfx.view.components;
    opens fr.chessproject.chessfx.view.components to javafx.fxml;
    exports fr.chessproject.chessfx.service;
    opens fr.chessproject.chessfx.service to javafx.fxml;
}