package snakeproject;
import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage stage) {

        ScreenManager screenManager = ScreenManager.getInstance();
        screenManager.init(stage);
        
        screenManager.addScreen("start", SceneBuilder.buildStartScene());
        screenManager.addScreen("game", SceneBuilder.buildGameScene());

        screenManager.setScreen("game");
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
