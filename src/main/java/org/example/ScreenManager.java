package org.example;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ScreenManager {

    private static ScreenManager instance;
    private final StackPane rootContainer;

    private ScreenManager() {
        this.rootContainer = new StackPane();
    }

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public StackPane getRoot() {
        return rootContainer;
    }

    public void switchScreen(Node newScreen) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(newScreen);
    }

    public void openOverlay(Node overlay) {

        if (!rootContainer.getChildren().contains(overlay)) {
            rootContainer.getChildren().add(overlay);
        }
    }

    public void closeOverlay(Node overlay) {
        rootContainer.getChildren().remove(overlay);
    }

    public void closeTopOverlay() {
        int size = rootContainer.getChildren().size();

        if (size > 1) {
            rootContainer.getChildren().remove(size - 1);
        }
    }
}