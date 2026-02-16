package org.example.controllers;

import java.util.ArrayList;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.example.SceneFactory;
import org.example.ScreenManager;
import org.example.utils.*;


public class GameLevelController {

    private class LayoutHandler implements ChangeListener<Number> {

        @Override
        public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
            double orient = Math.min(rootPane.getHeight(), rootPane.getWidth());
            double proportion = 0.80;
            double side = orient * proportion;
            gameCanvas.setWidth(side);
            gameCanvas.setHeight(side);

            javafx.application.Platform.runLater(() -> { render(); });
        }
    }
    
    private final LayoutHandler layoutHandler = new LayoutHandler();

    @FXML
    private StackPane rootPane;
    @FXML
    private StackPane overlayLayer;
    @FXML
    private Canvas gameCanvas;

    private AnimationTimer gameLoop;
    private GameRenderer gameRenderer = new GameRenderer();
    
    private int numberOfLines; 

    private Snake snake = new Snake();
    private Coordinate2D<Integer> food;
    private ArrayList<Coordinate2D<Integer>> freeSpots = new ArrayList<>();

    private double dragStartX;
    private double dragStartY;

    private void initFreeSpots() {
        freeSpots.clear(); 
        for (int i = 0; i <= numberOfLines; i++) {
            for (int j = 0; j <= numberOfLines; j++) {
                freeSpots.add(new Coordinate2D<>(i, j));
            }
        }
    }

    private Coordinate2D<Integer> chooseFreeSpot(){
        if(freeSpots.isEmpty()){
            return null;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(freeSpots.size());
        Coordinate2D<Integer> randomDuo = freeSpots.get(randomIndex);
        freeSpots.remove(randomIndex);
        return randomDuo;
    }

    private Coordinate2D<Integer> getValidNeighbor(Coordinate2D<Integer> head) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newX = head.getX() + dir[0];
            int newY = head.getY() + dir[1];
            
            if (newX >= 0 && newX <= numberOfLines && newY >= 0 && newY <= numberOfLines) {
                Coordinate2D<Integer> neighbor = new Coordinate2D<>(newX, newY);
                if (freeSpots.contains(neighbor)) {
                    return neighbor;
                }
            }
        }
        return null; 
    }

    @FXML
    public void initialize() {
        gameRenderer.init(gameCanvas.getGraphicsContext2D());

        rootPane.widthProperty().addListener(layoutHandler);
        rootPane.heightProperty().addListener(layoutHandler);

        gameCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win") || os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                    attachKeyEvent(newScene);
                } else {
                    attachSwipeEvent(newScene);
                }
            }
        });
    }

    public void startLevel(LevelSettings settings) {
        this.numberOfLines = settings.getNumberOfLines();

        if (settings.getCssPath() != null) {
            try {
                rootPane.getStylesheets().clear();
                String css = getClass().getResource(settings.getCssPath()).toExternalForm();
                rootPane.getStylesheets().add(css);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        gameRenderer.setTheme(
            settings.getGridColor1(),
            settings.getGridColor2(),
            settings.getGridLineColor(),
            settings.getSnakeColor()
        );

        snake = new Snake();
        initFreeSpots();

        Coordinate2D<Integer> head = chooseFreeSpot(); 
        Coordinate2D<Integer> tail = getValidNeighbor(head);
        if (tail != null) {
            freeSpots.remove(tail);
        }
        snake.init(head, tail);
        food = chooseFreeSpot();

        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        gameLoop.start();
        
        render();
    }

    private void attachKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                moveUp();
            } else if (event.getCode() == KeyCode.DOWN) {
                moveDown();
            } else if (event.getCode() == KeyCode.LEFT) {
                moveLeft();
            } else if (event.getCode() == KeyCode.RIGHT) {
                moveRight();
            }
        });
    }

    private void attachSwipeEvent(Scene scene) {
        scene.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
        });

        scene.setOnMouseReleased(event -> {
            double dragEndX = event.getSceneX();
            double dragEndY = event.getSceneY();
            
            double deltaX = dragEndX - dragStartX;
            double deltaY = dragEndY - dragStartY;

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > 30) {
                    if (deltaX > 0) {
                        moveRight();
                    } else {
                        moveLeft();
                    }
                }
            } else {
                if (Math.abs(deltaY) > 30) {
                    if (deltaY > 0) {
                        moveDown();
                    } else {
                        moveUp();
                    }
                }
            }
        });
    }

    private void moveUp() {
        Coordinate2D<Integer> head = snake.getHead();
        if (head.getY() > 0) {
            Coordinate2D<Integer> next = new Coordinate2D<>(head.getX(), head.getY() - 1);
            snake.setDirection("up");
            handleNextStep(next);
        }
    }

    private void moveDown() {
        Coordinate2D<Integer> head = snake.getHead();
        if (head.getY() < numberOfLines) { 
            Coordinate2D<Integer> next = new Coordinate2D<>(head.getX(), head.getY() + 1);
            snake.setDirection("down");
            handleNextStep(next);
        }
    }

    private void moveLeft() {
        Coordinate2D<Integer> head = snake.getHead();
        if (head.getX() > 0) {
            Coordinate2D<Integer> next = new Coordinate2D<>(head.getX() - 1, head.getY());
            snake.setDirection("left");
            handleNextStep(next);
        }
    }

    private void moveRight() {
        Coordinate2D<Integer> head = snake.getHead();
        if (head.getX() < numberOfLines) {
            Coordinate2D<Integer> next = new Coordinate2D<>(head.getX() + 1, head.getY());
            snake.setDirection("right");
            handleNextStep(next);
        }
    }

    private void render() {
        double sideLengthOfCanvas = gameCanvas.getHeight();
        if (numberOfLines == 0) return; 
        
        double spacing = sideLengthOfCanvas / (numberOfLines + 1);
        gameRenderer.drawField(sideLengthOfCanvas, numberOfLines);
        gameRenderer.drawSnake(snake, spacing);
        if (food != null) {
            if (numberOfLines == 2) { 
                gameRenderer.drawPear(spacing, food.getX(), food.getY());
            } else {
                gameRenderer.drawLilyPad(spacing, food.getX(), food.getY());
            }
        }
    }

    private void handleNextStep(Coordinate2D<Integer> next){
        if(snake.isBodyPart(next) && !(snake.getTail().equals(next))){
            gameLoop.stop();
            showGameOverOverlay();
            return;
        }

        if(next.equals(food)){
            snake.grow(next);
            render();
            SoundManager.playEat();
            food = chooseFreeSpot();
            
            if(food == null){
                render();
                gameLoop.stop();
                showNextOverlay();
                return;
            }
        } else {
            Coordinate2D<Integer> oldTail = snake.getTail();
            snake.move(next); 
            if (!next.equals(oldTail)) {
                freeSpots.remove(next);
                freeSpots.add(oldTail);
            }
        }
    }

    private void showNextOverlay() {
        SoundManager.playWin();
        ScreenManager.getInstance().openOverlay(SceneFactory.getGameNextOverlay());
    }

    private void showGameOverOverlay() {
        SoundManager.playGameOver();
        ScreenManager.getInstance().openOverlay(SceneFactory.getGameOverOverlay());
    }

}