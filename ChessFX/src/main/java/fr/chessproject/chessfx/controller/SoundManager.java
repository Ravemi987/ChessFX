package fr.chessproject.chessfx.controller;

import javafx.scene.media.AudioClip;

import java.util.Objects;

public class SoundManager {

    private static final AudioClip moveSound;
    private static final AudioClip captureSound;
    private static final AudioClip invalidMoveSound;

    static {
        moveSound = loadSound("/sounds/move.wav");
        captureSound = loadSound("/sounds/capture.wav");
        invalidMoveSound = loadSound("/sounds/illegal.wav");

        if (moveSound != null) {
            moveSound.setVolume(0);
            moveSound.play();
        }
        if (captureSound != null) {
            captureSound.setVolume(0);
            captureSound.play();
        }
        if( invalidMoveSound != null) {
            invalidMoveSound.setVolume(0);
            invalidMoveSound.play();
        }

        if (moveSound != null) moveSound.setVolume(1.0);
        if (captureSound != null) captureSound.setVolume(1.0);
        if (invalidMoveSound != null) invalidMoveSound.setVolume(1.0);
    }

    private static AudioClip loadSound(String path) {
        try {
            return new AudioClip(Objects.requireNonNull(SoundManager.class.getResource(path)).toExternalForm());
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + path);
            e.printStackTrace();
            return null;
        }
    }

    public static void playMoveSound() {
        if (moveSound != null) moveSound.play();
    }

    public static void playCaptureSound() {
        if (captureSound != null) captureSound.play();
    }

    public static void playInvalidMoveSound() {
        if (invalidMoveSound != null) invalidMoveSound.play();
    }
}
