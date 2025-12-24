package snakeproject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class SceneBuilder {

   
    public static Scene buildStartScene() {
        return buildScene("/fxml/startScene.fxml", "/css/startSceneStyle.css");
    }
   
    public static Scene buildGameScene() {
        return buildScene("/fxml/gameScene.fxml", "/css/gameSceneStyle.css");
    }

    private static Scene buildScene(String fxmlPath, String cssPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneBuilder.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            if (cssPath != null) {
                scene.getStylesheets().add(SceneBuilder.class.getResource(cssPath).toExternalForm());
            }

            return scene;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
