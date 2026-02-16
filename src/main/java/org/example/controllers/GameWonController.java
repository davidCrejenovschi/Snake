package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import org.example.SceneFactory;
import org.example.ScreenManager;
import org.example.utils.SoundManager;


public class GameWonController {

    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Button homeButton;

    @FXML
    public void initialize() {
        DoubleBinding cardWidth = rootPane.widthProperty().multiply(0.8);
        DoubleBinding cardHeight = rootPane.heightProperty().multiply(0.6);
        
        contentBox.prefWidthProperty().bind(cardWidth);
        contentBox.prefHeightProperty().bind(cardHeight);
        contentBox.setMaxWidth(Region.USE_PREF_SIZE);
        contentBox.setMaxHeight(Region.USE_PREF_SIZE);
        contentBox.spacingProperty().bind(cardHeight.multiply(0.12));
        contentBox.setStyle("-fx-alignment: CENTER; -fx-padding: 30;");

        NumberBinding titleFontSize = Bindings.min(cardWidth.multiply(0.09), cardHeight.multiply(0.15));
        titleLabel.styleProperty().bind(Bindings.concat("-fx-font-size: ", titleFontSize.asString(), "px;"));
        titleLabel.setMinWidth(Region.USE_PREF_SIZE);

        NumberBinding subtitleFontSize = titleFontSize.multiply(0.5);
        subtitleLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-size: ", subtitleFontSize.asString(), "px; ",
            "-fx-font-weight: bold;"
        ));
        subtitleLabel.setMinWidth(Region.USE_PREF_SIZE);

        DoubleBinding btnWidth = rootPane.widthProperty().multiply(0.3); 
        DoubleBinding btnHeight = rootPane.heightProperty().multiply(0.1);

        homeButton.prefWidthProperty().bind(btnWidth);
        homeButton.prefHeightProperty().bind(btnHeight);
        homeButton.setMinWidth(Region.USE_PREF_SIZE);
        homeButton.setMaxWidth(Region.USE_PREF_SIZE);

        NumberBinding btnFontSize = Bindings.min(
            btnHeight.multiply(0.4), 
            btnWidth.multiply(0.15)
        );

        homeButton.styleProperty().bind(Bindings.concat(
            "-fx-font-size: ", btnFontSize.asString(), "px;"
        ));

    }

    @FXML
    private void onHomeButtonClicked(){

        SoundManager.playClick();
        ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().switchScreen(SceneFactory.getGameMenu());
    }
}