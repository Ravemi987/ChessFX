module fr.chessproject.chessfx {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.swing;
    requires transitive jdk.compiler;
    requires transitive java.desktop;
    requires transitive javafx.media;
    requires transitive javafx.graphics;

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
    exports fr.chessproject.chessfx.model.UCI;
    opens fr.chessproject.chessfx.model.UCI to javafx.fxml;
}