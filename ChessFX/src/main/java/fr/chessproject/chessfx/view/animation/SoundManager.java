package fr.chessproject.chessfx.view.animation;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    private static Clip moveClip;
    private static Clip captureClip;
    private static Clip invalidClip;

    private static Clip loadClip(String path) {
        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) throw new IllegalArgumentException("Sound file not found: " + path);

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to load sound: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private static void muteClip(Clip clip) {
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(-80f); // silence
        } catch (IllegalArgumentException ignored) {}
    }

    private static void unmuteClip(Clip clip) {
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(0f); // volume normal
        } catch (IllegalArgumentException ignored) {}
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
        clip.setFramePosition(0);
        clip.start();
    }

    public static void preloadSounds() {
        moveClip = loadClip("/sounds/move.wav");
        captureClip = loadClip("/sounds/capture.wav");
        invalidClip = loadClip("/sounds/illegal.wav");

        muteClip(moveClip);
        muteClip(captureClip);
        muteClip(invalidClip);

        moveClip.setFramePosition(0);
        moveClip.start();
        moveClip.stop();

        captureClip.setFramePosition(0);
        captureClip.start();
        captureClip.stop();

        invalidClip.setFramePosition(0);
        invalidClip.start();
        invalidClip.stop();

        unmuteClip(moveClip);
        unmuteClip(captureClip);
        unmuteClip(invalidClip);
    }
}
