package fr.chessproject.chessfx.helpers;

import javafx.fxml.FXMLLoader;

import java.util.function.Consumer;

public class FXMLControllerLoader {

    public static <T> void loadFXMLController(Object cl, String fxmlPath, Consumer<T> controllerSetter) {
        try {
            FXMLLoader loader = new FXMLLoader(cl.getClass().getResource(fxmlPath));
            T controller = loader.getController();
            controllerSetter.accept(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
