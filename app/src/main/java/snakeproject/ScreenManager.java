package snakeproject;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class ScreenManager {

    private static ScreenManager instance; 
    private Stage primaryStage;
    private Map<String, Scene> screens = new HashMap<>();

    private ScreenManager() {}

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    public void addScreen(String name, Scene scene) {
        screens.put(name, scene);
    }

    public void setScreen(String name) {

        Scene scene = screens.get(name);
        if (scene != null && primaryStage != null) {
            primaryStage.setScene(scene);
        } else {
            System.err.println("Screen or Stage not found: " + name);
        }
    }
}
