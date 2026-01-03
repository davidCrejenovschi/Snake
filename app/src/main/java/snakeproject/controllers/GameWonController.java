package snakeproject.controllers;

import javafx.fxml.FXML;
import snakeproject.ScreenManager;

public class GameWonController {

    @FXML
    private void onHomeButtonClicked(){
        ScreenManager.getInstance().setScreen("start");
    }
    
}
