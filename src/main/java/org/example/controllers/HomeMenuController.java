package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.SceneFactory;
import org.example.ScreenManager;
import org.example.utils.AiManager;
import org.example.utils.LevelManager;
import org.example.utils.SoundManager;

public class HomeMenuController {

    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private Label titleLabel;
    @FXML private Button startButton;
    @FXML private ToggleButton aiToggleButton;

    @FXML
    public void initialize() {
        contentBox.spacingProperty().bind(rootPane.heightProperty().multiply(0.05));

        DoubleBinding btnWidth = rootPane.widthProperty().multiply(0.3);
        DoubleBinding btnHeight = rootPane.heightProperty().multiply(0.1);

        startButton.setMinWidth(0);
        startButton.setMinHeight(0);

        aiToggleButton.setMinWidth(0);  // <-- ADAUGĂ ASTA
        aiToggleButton.setMinHeight(0); // <-- ADAUGĂ ASTA

        startButton.prefWidthProperty().bind(btnWidth);
        startButton.prefHeightProperty().bind(btnHeight);


        NumberBinding fontSizeBinding = Bindings.min(
            btnHeight.multiply(0.4), 
            btnWidth.multiply(0.15)
        );


        aiToggleButton.prefWidthProperty().bind(btnWidth.multiply(0.8)); // Un pic mai mic decât Start
        aiToggleButton.prefHeightProperty().bind(btnHeight.multiply(0.6));
        aiToggleButton.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", fontSizeBinding.multiply(0.6).asString(), "px; ",
                "-fx-padding: 0;"
        ));

        startButton.styleProperty().bind(Bindings.concat(
            "-fx-font-size: ", fontSizeBinding.asString(), "px; ",
            "-fx-padding: 0;"
        ));

        titleLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-size: ", 
            Bindings.min(rootPane.widthProperty(), rootPane.heightProperty()).multiply(0.10).asString(), 
            "px;"
        ));
    }

    @FXML
    private void onStartButtonClicked() {

        SoundManager.playClick();
        LevelManager.setCurrentLevelIndex(0);
        Parent gameLevel = SceneFactory.createGameLevel(0);
        ScreenManager.getInstance().switchScreen(gameLevel);
    }

    @FXML
    private void onAiToggleClicked() {
        SoundManager.playClick();

        if (aiToggleButton.isSelected()) {
            aiToggleButton.setText("AI MODE: ON");
            AiManager.isAiEnabled = true;

            // 1. BLOCĂM BUTONUL DE START ȘI SCHIMBĂM TEXTUL
            startButton.setDisable(true);
            startButton.setText("LOADING AI...");

            // 2. CE SE ÎNTÂMPLĂ CÂND PYTHON E GATA:
            AiManager.onServerReady = () -> {
                startButton.setDisable(false);
                startButton.setText("START");
            };

            // 3. PORNIM SERVERUL
            AiManager.startPythonServer();

        } else {
            aiToggleButton.setText("AI MODE: OFF");
            AiManager.isAiEnabled = false;

            // Dacă se răzgândește și oprește AI-ul, oprim serverul și deblocăm butonul imediat
            AiManager.stopPythonServer();
            startButton.setDisable(false);
            startButton.setText("START");
        }
    }
}