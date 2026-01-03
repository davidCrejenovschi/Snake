package snakeproject.controllers;

import javafx.fxml.FXML;
import snakeproject.ScreenManager;

public class StartController {

    @FXML
    private void onStartButtonClicked(){
        ScreenManager.getInstance().setScreen("game");
    }
    
}
