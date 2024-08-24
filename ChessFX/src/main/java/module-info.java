module fr.chessproject.chessfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

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