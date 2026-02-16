package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import org.example.ScreenManager;
import org.example.SceneFactory;
import org.example.utils.LevelManager;
import org.example.utils.SoundManager;

public class GameNextController {

    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private Label titleLabel;
    @FXML private Button nextButton;
    @FXML private Button homeButton;

    @FXML
    public void initialize() {

        contentBox.spacingProperty().bind(rootPane.heightProperty().multiply(0.05));

        var btnWidth = rootPane.widthProperty().multiply(0.3);
        var btnHeight = rootPane.heightProperty().multiply(0.08);

        for (Button btn : new Button[]{nextButton, homeButton}) {
            btn.prefWidthProperty().bind(btnWidth);
            btn.prefHeightProperty().bind(btnHeight);

            btn.styleProperty().bind(Bindings.concat(
                    "-fx-font-size: ", btnHeight.multiply(0.4).asString(), "px;"
            ));
        }

        titleLabel.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                Bindings.min(rootPane.widthProperty(), rootPane.heightProperty()).multiply(0.1).asString(),
                "px;"
        ));
    }

    @FXML
    public void onHomeButtonClicked() {

        SoundManager.playClick();
        ScreenManager.getInstance().switchScreen(SceneFactory.getGameMenu());
    }

    @FXML
    public void onNextButtonClicked() {

        SoundManager.playClick();
        int nextIndex = LevelManager.getCurrentLevelIndex() + 1;

        if (nextIndex < LevelManager.getLevelCount()) {

          LevelManager.setCurrentLevelIndex(nextIndex);
          var nextLevelNode = SceneFactory.createGameLevel(nextIndex);
          ScreenManager.getInstance().switchScreen(nextLevelNode);

        } else {

            ScreenManager.getInstance().switchScreen(SceneFactory.getWonMenu());
        }
    }
}