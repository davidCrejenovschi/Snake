package snakeproject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import snakeproject.controllers.GameController;
import snakeproject.utils.LevelSettings;

import java.io.IOException;


public class ViewFactory {

    public static Parent buildStartView() {
        return loadView("/fxml/startView.fxml", "/css/startViewStyle.css");
    }
    
    public static Parent buildGameView(LevelSettings levelSettings) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewFactory.class.getResource("/fxml/gameView.fxml"));
            Parent root = loader.load();

            if (levelSettings.getCssPath() != null) {
                String css = ViewFactory.class.getResource(levelSettings.getCssPath()).toExternalForm();
                root.getStylesheets().clear();
                root.getStylesheets().add(css);
            }

            GameController controller = loader.getController();
            controller.startLevel(levelSettings);

            return root;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Parent buildGameOverView(){
        return loadView("/fxml/gameOverView.fxml", "/css/gameOverViewStyle.css");
    }

    public static Parent buildGameWonView(){
        return loadView("/fxml/gameWonView.fxml", "/css/gameWonViewStyle.css");
    } 

    private static Parent loadView(String fxmlPath, String cssPath) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewFactory.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (cssPath != null) {
                String css = ViewFactory.class.getResource(cssPath).toExternalForm();
                root.getStylesheets().add(css);
            }

            return root;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}