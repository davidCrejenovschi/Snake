package snakeproject.utils;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

public class LevelManager {

    private static final List<LevelSettings> levels = new ArrayList<>();

    static {
        levels.add(new LevelSettings(
            1, 
            2, 
            "/css/gameView1Style.css", 
            Color.web("#c7e098"), 
            Color.web("#94a86d"), 
            Color.web("#2E8B57"), 
            Color.web("#AED751")  
        ));

        levels.add(new LevelSettings(
            2, 
            5, 
            "/css/gameView2Style.css", 
            Color.web("#89CFF0"), 
            Color.web("#0096FF"), 
            Color.web("#0047AB"), 
            Color.web("#c173be")  
        ));
    }

    public static LevelSettings getLevel(int levelIndex) {
        if (levelIndex >= 0 && levelIndex < levels.size()) {
            return levels.get(levelIndex);
        }
        return levels.get(0); 
    }
    
    public static int getLevelCount() {
        return levels.size();
    }
}