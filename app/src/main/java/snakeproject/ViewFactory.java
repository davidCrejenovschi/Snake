package snakeproject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;


public class ViewFactory {

    public static Parent buildStartView() {
        return loadView("/fxml/startView.fxml", "/css/startViewStyle.css");
    }
    
    public static Parent buildGameView() {
        return loadView("/fxml/gameView.fxml", "/css/gameViewStyle.css");
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