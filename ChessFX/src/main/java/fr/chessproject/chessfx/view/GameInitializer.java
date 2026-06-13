package fr.chessproject.chessfx.view;

import fr.chessproject.chessfx.view.animation.SoundManager;

public class GameInitializer {

    public static void preloadAll(MainFrameController frameController) {
        frameController.preloadSprites();
        SoundManager.preloadSounds();
    }
}
