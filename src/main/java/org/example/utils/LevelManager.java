package org.example.utils;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

public class LevelManager {

    private static final List<LevelSettings> levels = new ArrayList<>();
    private static int currentLevelIndex = 0;

    static {
       
        levels.add(new LevelSettings(
            1, 
            2, 
            "/org/example/css/gameOneStyle.css",
            Color.web("#c7e098"), 
            Color.web("#94a86d"), 
            Color.web("#2E8B57"), 
            Color.web("#AED751")  
        ));

        levels.add(new LevelSettings(
            2, 
            3, 
            "/org/example/css/gameTwoStyle.css",
            Color.web("#89CFF0"), 
            Color.web("#0096FF"), 
            Color.web("#0047AB"), 
            Color.web("#3483eb")
        ));
    }

    public static LevelSettings getLevel(int levelIndex) {
        if (levelIndex >= 0 && levelIndex < levels.size()) {
            return levels.get(levelIndex);
        }
        return levels.getFirst();
    }
    
    public static int getLevelCount() {
        return levels.size();
    }

    public static int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public static void setCurrentLevelIndex(int index) {
        currentLevelIndex = index;
    }
}