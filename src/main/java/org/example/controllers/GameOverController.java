package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.SceneFactory;
import org.example.ScreenManager;
import org.example.utils.LevelManager;
import org.example.utils.SoundManager;

public class GameOverController {

    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private Label titleLabel;
    @FXML private Button retryButton;
    @FXML private Button homeButton;

    @FXML
    public void initialize() {

        rootPane.widthProperty().addListener((obs, old, val) -> resizeUI());
        rootPane.heightProperty().addListener((obs, old, val) -> resizeUI());

        Platform.runLater(this::resizeUI);
    }

    private void resizeUI() {
        double w = rootPane.getWidth();
        double h = rootPane.getHeight();
        if (w <= 0 || h <= 0) return;

        contentBox.setSpacing(h * 0.05);

        double btnW = w * 0.3;
        double btnH = h * 0.08;

        double fontSizeTitle = Math.min(w, h) * 0.12;
        double fontSizeBtn = btnH * 0.4;

        titleLabel.setStyle("-fx-font-size: " + fontSizeTitle + "px;");

        for (Button b : new Button[]{retryButton, homeButton}) {
            b.setPrefWidth(btnW);
            b.setPrefHeight(btnH);
            b.setStyle("-fx-font-size: " + fontSizeBtn + "px; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-color: white; -fx-border-width: 2px;");
        }
    }

    @FXML
    private void onRetryButtonClicked() {

        SoundManager.playClick();
        LevelManager.setCurrentLevelIndex(0);
        ScreenManager.getInstance().switchScreen(SceneFactory.createGameLevel(0));
    }

    @FXML
    private void onHomeButtonClicked() {

        SoundManager.playClick();
        ScreenManager.getInstance().switchScreen(SceneFactory.getGameMenu());
    }
}