package org.example.utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class SoundManager {

    public static void playClick() {
        playSound("/org/example/sounds/click.wav");
    }

    public static void playGameOver() {
        playSound("/org/example/sounds/game_over.wav");
    }

    public static void playEat() {
        playSound("/org/example/sounds/eat.wav");
    }

    public static void playWin() {
        playSound("/org/example/sounds/win.wav");
    }

    private static void playSound(String path) {
        try {
            URL soundURL = SoundManager.class.getResource(path);

            if (soundURL == null) {
                System.err.println("Audio not found: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();

        } catch (Exception ignored) {}
    }
}