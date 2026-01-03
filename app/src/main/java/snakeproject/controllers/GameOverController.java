package snakeproject.controllers;

import javafx.fxml.FXML;
import snakeproject.ScreenManager;
import snakeproject.ViewFactory;

public class GameOverController {

    @FXML
    private void onPlayAgainButtonClicked(){
       
        ScreenManager screenManager = ScreenManager.getInstance();
        screenManager.addScreen("game", ViewFactory.buildGameView());
        screenManager.setScreen("game");
    }    
    
}
