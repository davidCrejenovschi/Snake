package snakeproject.controllers;
import javafx.fxml.FXML;
import snakeproject.ScreenManager;


public class GameWonController {

    @FXML
    private void onHomeButtonClicked(){
        ScreenManager screenManager = ScreenManager.getInstance();
        screenManager.setScreen("start");
    }
    
}
