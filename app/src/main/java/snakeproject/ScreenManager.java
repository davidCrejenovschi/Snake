package snakeproject;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class ScreenManager {

    private static ScreenManager instance;
    private Stage stage;
    private final Map<String, Parent> roots = new HashMap<>();

    private ScreenManager() {}

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public void init(Stage stage) {
        this.stage = stage;
    }

    public void addScreen(String name, Parent root) {
        roots.put(name, root);
    }

    public void setScreen(String name) {
        
        Parent root = roots.get(name);
        if (root == null) return;

        if (stage.getScene() == null) {
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }
    }
}