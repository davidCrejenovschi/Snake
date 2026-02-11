package snakeproject.controllers;

import javafx.fxml.FXML;
import snakeproject.ScreenManager;
import snakeproject.ViewFactory;
import snakeproject.utils.LevelManager;
import snakeproject.utils.LevelSettings;

public class GameOverController {

    @FXML
    private void onPlayAgainButtonClicked(){
        ScreenManager screenManager = ScreenManager.getInstance();
        
        LevelSettings settings = LevelManager.getLevel(0); 
        
        screenManager.addScreen("game", ViewFactory.buildGameView(settings));
        screenManager.setScreen("game");
    } 
    
}
