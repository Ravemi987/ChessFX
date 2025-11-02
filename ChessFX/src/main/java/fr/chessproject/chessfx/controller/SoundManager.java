package fr.chessproject.chessfx.controller;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    private static final Clip moveClip;
    private static final Clip captureClip;
    private static final Clip invalidClip;

    static {
        moveClip = loadClip("/sounds/move.wav");
        captureClip = loadClip("/sounds/capture.wav");
        invalidClip = loadClip("/sounds/illegal.wav");
    }

    private static Clip loadClip(String path) {
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) throw new IllegalArgumentException("Sound file not found: " + path);

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais); // précharge en mémoire
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to load sound: " + path);
            e.printStackTrace();
            return null;
        }
    }

    public static void playMoveSound() {
        playClip(moveClip);
    }

    public static void playCaptureSound() {
        playClip(captureClip);
    }

    public static void playInvalidMoveSound() {
        playClip(invalidClip);
    }

    private static void playClip(Clip clip) {
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(20);
        clip.start();
    }
}
