package org.example;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.controllers.GameLevelController;
import org.example.utils.LevelManager;
import org.example.utils.LevelSettings;

import java.io.IOException;
import java.net.URL;

public class SceneFactory {

    private static Parent homeMenu;

    public static Parent getGameMenu() {
        if (homeMenu == null) {

            homeMenu = loadFXMLAndCSS(
                    "/org/example/fxml/gameHomeView.fxml",
                    "/org/example/css/gameHomeStyle.css"
            );
        }
        return homeMenu;
    }

    public static Parent getGameNextOverlay() {
        return loadFXMLAndCSS(
                "/org/example/fxml/gameNextView.fxml",
                "/org/example/css/gameNextStyle.css"
        );
    }

    public static Parent getWonMenu(){

        return loadFXMLAndCSS("/org/example/fxml/gameWonView.fxml", "/org/example/css/gameWonStyle.css");
    }

    public static Parent getGameOverOverlay() {
        return loadFXMLAndCSS(
                "/org/example/fxml/gameOverView.fxml",
                "/org/example/css/gameOverStyle.css"
        );
    }

    public static Parent createGameLevel(int levelIndex) {
        try {

            LevelSettings settings = LevelManager.getLevel(levelIndex);
            FXMLLoader loader = new FXMLLoader(SceneFactory.class.getResource("/org/example/fxml/gameUniversalView.fxml"));
            Parent root = loader.load();

            GameLevelController controller = loader.getController();

            controller.startLevel(settings);

            return root;

        } catch (IOException e) {
            throw new RuntimeException("Nu s-a putut crea nivelul " + levelIndex, e);
        }
    }

    private static Parent loadFXMLAndCSS(String fxmlPath, String cssPath) {
        try {
            URL fxmlUrl = SceneFactory.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML file not found at: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            if (cssPath != null && !cssPath.isEmpty()) {
                URL cssUrl = SceneFactory.class.getResource(cssPath);
                if (cssUrl != null) {
                    root.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("Warning: CSS file not found at: " + cssPath);
                }
            }

            return root;

        } catch (IOException e) {
            throw new RuntimeException("Critical error loading FXML view: " + fxmlPath, e);
        }
    }
}