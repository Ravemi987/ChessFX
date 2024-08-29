module fr.chessproject.chessfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires jdk.compiler;
    requires java.desktop;

    exports fr.chessproject.chessfx.controller;
    opens fr.chessproject.chessfx.controller to javafx.fxml;
    exports fr.chessproject.chessfx.helpers;
    opens fr.chessproject.chessfx.helpers to javafx.fxml;
    exports fr.chessproject.chessfx.main;
    opens fr.chessproject.chessfx.main to javafx.fxml;
    exports fr.chessproject.chessfx.model;
    opens fr.chessproject.chessfx.model to javafx.fxml;
    exports fr.chessproject.chessfx.view;
    opens fr.chessproject.chessfx.view to javafx.fxml;
}