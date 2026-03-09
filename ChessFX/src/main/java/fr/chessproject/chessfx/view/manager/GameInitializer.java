package fr.chessproject.chessfx.view.manager;

import fr.chessproject.chessfx.view.MainFrameController;

public class GameInitializer {

    public static void preloadAll(MainFrameController frameController) {
        preloadGraphics(frameController);
        preloadSounds();
    }

    private static void preloadGraphics(MainFrameController frameController) {
        frameController.preloadSprites();
    }

    private static void preloadSounds() {
        SoundManager.preloadSounds();
    }
}
