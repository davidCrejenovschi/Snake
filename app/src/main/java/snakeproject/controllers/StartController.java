package snakeproject.controllers;

import javafx.fxml.FXML;
import snakeproject.ScreenManager;
import snakeproject.ViewFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StartController {

    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private Label titleLabel;
    @FXML private Button startButton;

    @FXML
    public void initialize() {
        contentBox.spacingProperty().bind(rootPane.heightProperty().multiply(0.05));

        DoubleBinding btnWidth = rootPane.widthProperty().multiply(0.3);
        DoubleBinding btnHeight = rootPane.heightProperty().multiply(0.1);

        startButton.setMinWidth(0);
        startButton.setMinHeight(0);

        startButton.prefWidthProperty().bind(btnWidth);
        startButton.prefHeightProperty().bind(btnHeight);

        NumberBinding fontSizeBinding = Bindings.min(
            btnHeight.multiply(0.4), 
            btnWidth.multiply(0.15)
        );

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
    private void onStartButtonClicked(){
        ScreenManager screenManager = ScreenManager.getInstance();
        screenManager.addScreen("game", ViewFactory.buildGameView());
        screenManager.setScreen("game");
    }
}