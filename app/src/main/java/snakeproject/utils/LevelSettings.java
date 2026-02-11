package snakeproject.utils;

import javafx.scene.paint.Color;

public class LevelSettings {

    private final int levelNumber;
    private final int numberOfLines;
    private final String cssPath;
    private final Color gridColor1;
    private final Color gridColor2;
    private final Color gridLineColor;
    private final Color snakeColor;

    public LevelSettings(int levelNumber, int numberOfLines, String cssPath, 
                         Color gridColor1, Color gridColor2, Color gridLineColor, Color snakeColor) {
        this.levelNumber = levelNumber;
        this.numberOfLines = numberOfLines;
        this.cssPath = cssPath;
        this.gridColor1 = gridColor1;
        this.gridColor2 = gridColor2;
        this.gridLineColor = gridLineColor;
        this.snakeColor = snakeColor;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public String getCssPath() {
        return cssPath;
    }

    public Color getGridColor1() {
        return gridColor1;
    }

    public Color getGridColor2() {
        return gridColor2;
    }

    public Color getGridLineColor() {
        return gridLineColor;
    }

    public Color getSnakeColor() {
        return snakeColor;
    }
}