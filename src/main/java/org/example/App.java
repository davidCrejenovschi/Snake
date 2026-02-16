package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage stage) {

        ScreenManager screenManager = ScreenManager.getInstance();
        screenManager.switchScreen(SceneFactory.getGameMenu());
        Scene scene = new Scene(screenManager.getRoot());
        stage.setTitle("Snake Adventure");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
